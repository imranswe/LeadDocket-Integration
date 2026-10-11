package com.example.leaddocket;


import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@RestController
@RequestMapping("/api/webhooks/inbox")
public class InboxController {
 private final WebhookInbox inbox;
 public InboxController(WebhookInbox inbox){this.inbox=inbox;}
 @GetMapping public Object recent(){return inbox.recent();}
 @PostMapping("/{id}/retry")public Object retry(@PathVariable String id){
  if(!inbox.retry(id))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only DEAD events can be retried");
  return Map.of("queued",true);
 }
}
