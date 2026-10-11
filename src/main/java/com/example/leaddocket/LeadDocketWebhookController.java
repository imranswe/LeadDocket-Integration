package com.example.leaddocket;


import java.util.*;import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
@RestController
public class LeadDocketWebhookController {
 private final LeadDocketProperties p;private final WebhookSettings settings;private final JsonSupport json;private final WebhookInbox inbox;
 public LeadDocketWebhookController(LeadDocketProperties p,WebhookSettings settings,JsonSupport json,WebhookInbox inbox){this.p=p;this.settings=settings;this.json=json;this.inbox=inbox;}
 @PostMapping("/webhooks/leaddocket")
 public Object receive(HttpServletRequest request,@RequestHeader(value="x-leaddocket-signature",required=false)String signature)throws IOException{
  if(p.webhookKey().isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Configure webhook key");
  byte[] bytes=BodySupport.read(request,settings.maxBodyBytes());
  if(!WebhookSignature.valid(p.webhookKey(),bytes,signature))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid webhook signature");
  String payload=BodySupport.utf8(bytes);var event=json.parse(payload);
  if(event==null||!event.isObject())throw new IllegalArgumentException("Expected JSON object");
  String id;
  if(!p.eventIdField().isBlank()){
   id=event.path(p.eventIdField()).asText("");
   if(!id.matches("[A-Za-z0-9._:-]{1,200}"))throw new IllegalArgumentException("Missing or invalid configured event ID");
  }else if("NONE".equals(p.deduplicationMode()))id=UUID.randomUUID().toString();
  else if("PAYLOAD_HASH".equals(p.deduplicationMode()))id=BodySupport.hash(bytes);
  else throw new IllegalStateException("Unknown deduplication mode");
  return Map.of("received",true,"eventId",id,"duplicate",!inbox.receive(id,payload));
 }
}
