package com.example.leaddocket;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class AuditEventHandler implements WebhookEventHandler {
 private final JdbcTemplate db;
 public AuditEventHandler(JdbcTemplate db){this.db=db;}
 public void handle(String id,JsonNode event){
  db.update("INSERT INTO integration_event (event_id,event_type,resource_id,handled_at) VALUES (?,?,?,CURRENT_TIMESTAMP)",
   id,event.path("EventType").asText("Unknown"),event.path("LeadDocketLeadId").asText(event.path("LeadDocketContactId").asText("")));
 }
}
