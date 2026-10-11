package com.example.leaddocket;


import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.*;import java.nio.charset.*;
import java.security.MessageDigest;import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
public final class BodySupport {
 public static byte[] read(HttpServletRequest request,int max) throws IOException {
  byte[] body=request.getInputStream().readNBytes(max+1);
  if(body.length>max)throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
  return body;
 }
 public static String utf8(byte[] bytes) {
  try{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
   .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();}
  catch(CharacterCodingException e){throw new IllegalArgumentException("Body must be valid UTF-8");}
 }
 public static String hash(byte[] body) {
  try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));}
  catch(Exception e){throw new IllegalStateException(e);}
 }
 public static boolean matches(String expected,String actual) {
  return actual!=null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8));
 }
 private BodySupport() {}
}
