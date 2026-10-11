package com.example.leaddocket;
import java.net.URI;import java.util.*;
import org.springframework.web.util.UriComponentsBuilder;
final class Urls {
 static URI build(String base,String path,Map<String,?> parameters,Map<String,List<String>> query){
  var builder=UriComponentsBuilder.fromUriString(base).path(path);
  var variables=new LinkedHashMap<String,Object>();if(parameters!=null)variables.putAll(parameters);
  if(query!=null)for(var entry:query.entrySet()){
   if(entry.getKey().isBlank()||entry.getKey().contains("{")||entry.getKey().contains("}"))throw new IllegalArgumentException("Invalid query key");
   for(String item:entry.getValue()){
    String slot="__query_"+variables.size();while(variables.containsKey(slot))slot+="_";
    variables.put(slot,item);builder.queryParam(entry.getKey(),"{"+slot+"}");
   }
  }
  return builder.encode().buildAndExpand(variables).toUri();
 }
 private Urls(){}
}
