package com.example.leaddocket;


import java.net.URI;import java.net.http.*;import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={
 "spring.datasource.url=jdbc:h2:mem:leaddocket-tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "app.admin.password=local-admin-test-password","webhook.worker-enabled=false","leaddocket.webhook-key=local-webhook-key"})
class WebhookIntegrationTest {
 @Value("${local.server.port}")int port;@Autowired JdbcTemplate db;@Autowired WebhookInbox inbox;
 final HttpClient http=HttpClient.newHttpClient();final String body="{\"EventType\":\"Lead Created\",\"LeadDocketLeadId\":123}";
 String base(){return "http://localhost:"+port;}
 @BeforeEach void before(){db.update("DELETE FROM integration_event");db.update("DELETE FROM webhook_inbox");}
 HttpResponse<String> post(String payload,boolean authenticated)throws Exception{
  var r=HttpRequest.newBuilder(URI.create(base()+"/webhooks/leaddocket")).header("Content-Type","application/json");
  if(authenticated){r.header("x-leaddocket-signature",java.util.HexFormat.of().formatHex(WebhookSignature.sign("local-webhook-key",payload.getBytes(java.nio.charset.StandardCharsets.UTF_8))));}
  return http.send(r.POST(HttpRequest.BodyPublishers.ofString(payload)).build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test void rejectsUnsignedDelivery()throws Exception{
  assertThat(post(body,false).statusCode()).isEqualTo(401);assertThat(db.queryForObject("SELECT COUNT(*) FROM webhook_inbox",Integer.class)).isZero();
 }
 @Test void storesAndDeduplicates()throws Exception{
  assertThat(post(body,true).statusCode()).isEqualTo(200);assertThat(post(body,true).body()).contains("\"duplicate\":true");
  assertThat(db.queryForObject("SELECT COUNT(*) FROM webhook_inbox",Integer.class)).isEqualTo(1);
 }
 @Test void rejectsMalformedAndOversizedBodies()throws Exception{
  assertThat(post("not-json",true).statusCode()).isEqualTo(400);
  assertThat(post(" ".repeat(1048577),true).statusCode()).isEqualTo(413);
 }
 @Test void administrationRequiresBasicAuthentication()throws Exception{
  var r=HttpRequest.newBuilder(URI.create(base()+"/api/webhooks/inbox")).GET();
  assertThat(http.send(r.build(),HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
  String auth=Base64.getEncoder().encodeToString("admin:local-admin-test-password".getBytes());
  assertThat(http.send(r.header("Authorization","Basic "+auth).build(),HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(200);
 }
 @Test void workerProcessesAtomically(){
  inbox.receive("test-event",body);var tx=new org.springframework.jdbc.datasource.DataSourceTransactionManager(db.getDataSource());
  var worker=new WebhookWorker(inbox,new WebhookSettings(1048576,5,true),new JsonSupport(),new AuditEventHandler(db),tx,db);
  worker.poll();worker.poll();
  assertThat(db.queryForObject("SELECT status FROM webhook_inbox WHERE event_id='test-event'",String.class)).isEqualTo("PROCESSED");
  assertThat(db.queryForObject("SELECT COUNT(*) FROM integration_event",Integer.class)).isEqualTo(1);
 }
 @Test void workerRollsBackAndAllowsDeadLetterRetry(){
  inbox.receive("test-event",body);var tx=new org.springframework.jdbc.datasource.DataSourceTransactionManager(db.getDataSource());
  var worker=new WebhookWorker(inbox,new WebhookSettings(1048576,1,true),new JsonSupport(),(id,e)->{new AuditEventHandler(db).handle(id,e);throw new RuntimeException();},tx,db);
  worker.poll();assertThat(db.queryForObject("SELECT COUNT(*) FROM integration_event",Integer.class)).isZero();
  assertThat(db.queryForObject("SELECT status FROM webhook_inbox WHERE event_id='test-event'",String.class)).isEqualTo("DEAD");
  assertThat(inbox.retry("test-event")).isTrue();
 }

 @Test void rejectsBodyTampering()throws Exception{
  String signature=java.util.HexFormat.of().formatHex(WebhookSignature.sign("local-webhook-key",body.getBytes()));
  var r=HttpRequest.newBuilder(URI.create(base()+"/webhooks/leaddocket")).header("x-leaddocket-signature",signature)
   .POST(HttpRequest.BodyPublishers.ofString(body.replace("123","124"))).build();
  assertThat(http.send(r,HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(401);
 }
 @Test void hmacMatchesKnownVector(){
  assertThat(java.util.HexFormat.of().formatHex(WebhookSignature.sign("Jefe","what do ya want for nothing?".getBytes())))
   .isEqualTo("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843");
 }

}
