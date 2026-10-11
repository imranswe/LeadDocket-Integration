package com.example.leaddocket;


import com.sun.net.httpserver.*;import java.net.*;import java.util.*;import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
class LeadDocketClientTest {
 HttpServer server;String base;int calls;
 @BeforeEach void start()throws Exception{server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);base="http://127.0.0.1:"+server.getAddress().getPort();server.start();}
 @AfterEach void stop(){server.stop(0);}
 LeadDocketProperties properties(String mode,Map<String,LeadDocketProperties.Operation> ops){return new LeadDocketProperties(base,"secret+&",mode,"api_key",base+"/intake","key","","PAYLOAD_HASH",ops);}
 @Test void submitsFormWithoutRestKey(){
  server.createContext("/intake",e->{assertThat(e.getRequestHeaders().getFirst("api_key")).isNull();assertThat(new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8)).contains("First=Example","Summary=a%2Bb%26c");reply(e,200,"{\"success\":true,\"opportunityId\":42}");});
  var client=new LeadDocketClient(properties("UNCONFIGURED",Map.of()),new JsonSupport());
  assertThat(client.submitOpportunity(Map.of("First","Example","Summary","a+b&c"))).contains("opportunityId");
 }
 @Test void usesConfiguredHeaderAndEscapesPathVariables(){
  server.createContext("/contract",e->{assertThat(e.getRequestHeaders().getFirst("api_key")).isEqualTo("secret+&");assertThat(e.getRequestURI().getRawPath()).isEqualTo("/contract/items/a%2Fb");reply(e,200,"{}");});
  var client=new LeadDocketClient(properties("HEADER",Map.of("read",new LeadDocketProperties.Operation("GET","/contract/items/{id}","application/json"))),new JsonSupport());
  assertThat(client.invoke("read",new LeadDocketClient.ApiRequest(Map.of("id","a/b"),Map.of(),null))).isEqualTo("{}");
 }
 @Test void usesConfiguredQueryKey(){
  server.createContext("/contract",e->{assertThat(e.getRequestURI().getRawQuery()).contains("api_key=secret%2B%26");reply(e,200,"{}");});
  var client=new LeadDocketClient(properties("QUERY",Map.of("read",new LeadDocketProperties.Operation("GET","/contract","application/json"))),new JsonSupport());
  client.invoke("read",new LeadDocketClient.ApiRequest(Map.of(),Map.of("page",List.of("1")),null));
 }
 @Test void refusesUnconfiguredAuthAndAbsoluteOperationPaths(){
  var op=Map.of("read",new LeadDocketProperties.Operation("GET","/contract","application/json"));
  assertThatThrownBy(()->new LeadDocketClient(properties("UNCONFIGURED",op),new JsonSupport()).invoke("read",new LeadDocketClient.ApiRequest(null,null,null))).isInstanceOf(IllegalStateException.class);
  var bad=Map.of("read",new LeadDocketProperties.Operation("GET","//other.example/","application/json"));
  assertThatThrownBy(()->new LeadDocketClient(properties("HEADER",bad),new JsonSupport()).invoke("read",new LeadDocketClient.ApiRequest(null,null,null))).isInstanceOf(IllegalStateException.class);
 }
 @Test void neverAutomaticallyReplaysIntakeWrites(){
  server.createContext("/intake",e->{calls++;reply(e,503,"{}");});
  assertThatThrownBy(()->new LeadDocketClient(properties("UNCONFIGURED",Map.of()),new JsonSupport()).submitOpportunity(Map.of("First","Test"))).isInstanceOf(org.springframework.web.client.RestClientResponseException.class);
  assertThat(calls).isEqualTo(1);
 }
 static void reply(HttpExchange e,int status,String body)throws java.io.IOException{var bytes=body.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json");e.sendResponseHeaders(status,bytes.length);e.getResponseBody().write(bytes);e.close();}
}
