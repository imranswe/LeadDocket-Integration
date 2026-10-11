package com.example.leaddocket;


import java.net.URI;import java.util.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ResponseStatusException;
@Service
public class LeadDocketClient {
 private final LeadDocketProperties p;private final JsonSupport json;private final RestClient http=HttpClients.create();
 public LeadDocketClient(LeadDocketProperties p,JsonSupport json){this.p=p;this.json=json;}
 public String submitOpportunity(Map<String,String> fields){
  if(blank(p.opportunityUrl()))throw new IllegalStateException("Configure opportunity URL");
  var form=new LinkedMultiValueMap<String,String>();fields.forEach(form::add);
  // Verified form integration: this URL identifies the source and does not use the REST API key.
  return http.post().uri(p.opportunityUrl()).contentType(MediaType.APPLICATION_FORM_URLENCODED)
   .body(form).retrieve().body(String.class);
 }
 public String invoke(String operation,ApiRequest request){
  var op=p.operations()==null?null:p.operations().get(operation);
  if(op==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Configure this named REST operation from your tenant API console");
  if(blank(p.apiBaseUrl())||blank(p.apiKey())||blank(p.apiKeyName()))throw new IllegalStateException("Configure REST credentials");
  if(!Set.of("HEADER","QUERY").contains(p.apiAuthMode()))throw new IllegalStateException("Choose API auth placement from your tenant console");
  if(op.path()==null||!op.path().startsWith("/")||op.path().startsWith("//")||op.path().contains("..")||op.path().contains(":")||op.path().contains("?")||op.path().contains("#"))
   throw new IllegalStateException("Operation path must be a relative path without query or traversal");
  HttpMethod method=HttpMethod.valueOf(op.method().toUpperCase(Locale.ROOT));
  if(!Set.of(HttpMethod.GET,HttpMethod.POST,HttpMethod.PUT,HttpMethod.PATCH,HttpMethod.DELETE).contains(method))throw new IllegalStateException("Unsupported operation method");
  var query=new LinkedHashMap<String,List<String>>();
  if(request.query()!=null)request.query().forEach((k,v)->{
   if(k.equalsIgnoreCase(p.apiKeyName()))throw new IllegalArgumentException("API key must come from server configuration");query.put(k,v);
  });
  if(p.apiAuthMode().equals("QUERY"))query.put(p.apiKeyName(),List.of(p.apiKey()));
  URI url=Urls.build(p.apiBaseUrl(),op.path(),request.pathParameters(),query);
  var spec=http.method(method).uri(url).accept(MediaType.APPLICATION_JSON);
  if(p.apiAuthMode().equals("HEADER"))spec.header(p.apiKeyName(),p.apiKey());
  if(request.body()!=null){
   String type=blank(op.contentType())?MediaType.APPLICATION_JSON_VALUE:op.contentType();
   if(type.equals(MediaType.APPLICATION_JSON_VALUE))spec.contentType(MediaType.APPLICATION_JSON).body(json.write(request.body()));
   else if(type.equals(MediaType.APPLICATION_FORM_URLENCODED_VALUE)){
    if(!(request.body() instanceof Map<?,?> values))throw new IllegalArgumentException("Form body must be an object");
    var form=new LinkedMultiValueMap<String,String>();values.forEach((k,v)->form.add(String.valueOf(k),String.valueOf(v)));
    spec.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form);
   }else throw new IllegalStateException("Unsupported operation content type");
  }
  // No write or read retries: callers schedule rate-limit/availability recovery explicitly.
  return spec.retrieve().body(String.class);
 }
 private boolean blank(String s){return s==null||s.isBlank();}
 public record ApiRequest(Map<String,String> pathParameters,Map<String,List<String>> query,Object body){}
}
