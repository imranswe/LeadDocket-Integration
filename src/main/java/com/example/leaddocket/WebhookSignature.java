package com.example.leaddocket;


import java.nio.charset.StandardCharsets;import java.security.MessageDigest;import java.util.HexFormat;
import javax.crypto.Mac;import javax.crypto.spec.SecretKeySpec;
public final class WebhookSignature {
 public static boolean valid(String key,byte[] body,String signature){
  if(signature==null||!signature.matches("[0-9a-fA-F]{64}"))return false;
  return MessageDigest.isEqual(sign(key,body),HexFormat.of().parseHex(signature));
 }
 public static byte[] sign(String key,byte[] body){
  try{var mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return mac.doFinal(body);}
  catch(Exception e){throw new IllegalStateException("Cannot calculate webhook signature",e);}
 }
 private WebhookSignature(){}
}
