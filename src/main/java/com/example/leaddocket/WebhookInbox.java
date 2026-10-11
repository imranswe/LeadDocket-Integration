package com.example.leaddocket;


import java.nio.charset.StandardCharsets;import java.time.Instant;import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DuplicateKeyException;
@Repository
public class WebhookInbox {
 private final JdbcTemplate db;
 public WebhookInbox(JdbcTemplate db){this.db=db;}
 public boolean receive(String id,String payload) {
  String hash=BodySupport.hash(payload.getBytes(StandardCharsets.UTF_8));
  try{
   db.update("INSERT INTO webhook_inbox (event_id,payload,payload_hash,status,attempts,received_at,next_attempt_at) VALUES (?,?,?,'PENDING',0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",id,payload,hash);return true;
  }catch(DuplicateKeyException e){
   String stored=db.queryForObject("SELECT payload_hash FROM webhook_inbox WHERE event_id=?",String.class,id);
   if(!hash.equals(stored))throw new IllegalArgumentException("Event ID reused with different payload");return false;
  }
 }
 public List<Map<String,Object>> pending(){
  return db.query("SELECT event_id,attempts FROM webhook_inbox WHERE status='PENDING' AND next_attempt_at<=CURRENT_TIMESTAMP ORDER BY received_at LIMIT 20",
   (rs,n)->Map.<String,Object>of("id",rs.getString("event_id"),"attempts",rs.getInt("attempts")));
 }
 public void done(String id){db.update("UPDATE webhook_inbox SET status='PROCESSED',processed_at=CURRENT_TIMESTAMP WHERE event_id=?",id);}
 public void failed(String id,int attempts,int max){
  db.update("UPDATE webhook_inbox SET attempts=?,status=?,next_attempt_at=?,last_error=? WHERE event_id=? AND status='PENDING'",
   attempts,attempts>=max?"DEAD":"PENDING",java.sql.Timestamp.from(Instant.now().plusSeconds(Math.min(3600,1L<<Math.min(attempts,12)))),"Event handler failed",id);
 }
 public Object recent(){return db.queryForList("SELECT event_id,status,attempts,received_at,processed_at,last_error FROM webhook_inbox ORDER BY received_at DESC LIMIT 100");}
 public boolean retry(String id){return db.update("UPDATE webhook_inbox SET status='PENDING',attempts=0,next_attempt_at=CURRENT_TIMESTAMP,last_error=NULL WHERE event_id=? AND status='DEAD'",id)>0;}
}
