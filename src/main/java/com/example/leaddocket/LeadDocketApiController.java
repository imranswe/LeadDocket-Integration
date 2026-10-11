package com.example.leaddocket;


import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
@RestController
@RequestMapping(value="/api/leaddocket",produces=MediaType.APPLICATION_JSON_VALUE)
public class LeadDocketApiController {
 private final LeadDocketClient client;
 public LeadDocketApiController(LeadDocketClient client){this.client=client;}
 @PostMapping("/opportunities")public String intake(@RequestBody Map<String,String> fields){return client.submitOpportunity(fields);}
 @PostMapping("/operations/{name}")public String operation(@PathVariable String name,@RequestBody LeadDocketClient.ApiRequest request){return client.invoke(name,request);}
}
