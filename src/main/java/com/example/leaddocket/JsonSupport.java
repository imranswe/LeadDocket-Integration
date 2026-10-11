package com.example.leaddocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
@Component
public class JsonSupport {
 final ObjectMapper mapper = new ObjectMapper();
 public JsonNode parse(String s) {
  try { return mapper.readTree(s); } catch(Exception e) { throw new IllegalArgumentException("Invalid JSON"); }
 }
 public String write(Object o) {
  try { return mapper.writeValueAsString(o); } catch(Exception e) { throw new IllegalArgumentException("Cannot encode JSON"); }
 }
}
