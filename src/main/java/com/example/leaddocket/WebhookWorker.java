package com.example.leaddocket;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.slf4j.LoggerFactory;
@Component
public class WebhookWorker {
 private final WebhookInbox inbox;private final WebhookSettings settings;private final JsonSupport json;
 private final WebhookEventHandler handler;private final TransactionTemplate tx;private final JdbcTemplate db;
 public WebhookWorker(WebhookInbox inbox,WebhookSettings settings,JsonSupport json,WebhookEventHandler handler,PlatformTransactionManager manager,JdbcTemplate db){
  this.inbox=inbox;this.settings=settings;this.json=json;this.handler=handler;this.tx=new TransactionTemplate(manager);this.db=db;
 }
 @Scheduled(fixedDelayString="${webhook.poll-ms:2000}")
 public void poll(){
  if(!settings.workerEnabled())return;
  for(var candidate:inbox.pending()){
   String id=(String)candidate.get("id");
   try{tx.executeWithoutResult(s->{
    var row=db.queryForObject("SELECT payload,status FROM webhook_inbox WHERE event_id=? FOR UPDATE",(rs,n)->java.util.Map.of("payload",rs.getString("payload"),"status",rs.getString("status")),id);
    if(!"PENDING".equals(row.get("status")))return;
    handler.handle(id,json.parse(row.get("payload")));inbox.done(id);
   });}catch(Exception e){
    int attempt=((Number)candidate.get("attempts")).intValue()+1;inbox.failed(id,attempt,settings.maxAttempts());
    LoggerFactory.getLogger(getClass()).warn("Event {} failed, attempt {}, error type {}",id,attempt,e.getClass().getSimpleName());
   }
  }
 }
}
