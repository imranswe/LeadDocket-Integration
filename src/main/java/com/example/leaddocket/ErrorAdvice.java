package com.example.leaddocket;


import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
@RestControllerAdvice
public class ErrorAdvice {
 @ExceptionHandler(RestClientResponseException.class)
 public ResponseEntity<ProblemDetail> upstream(RestClientResponseException e) {
  var p=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,"Upstream request failed");
  p.setProperty("upstreamStatus",e.getStatusCode().value());
  var response=ResponseEntity.status(HttpStatus.BAD_GATEWAY);
  if(e.getStatusCode().value()==429 && e.getResponseHeaders()!=null) {
   String retry=e.getResponseHeaders().getFirst("Retry-After");if(retry!=null) response.header("Retry-After",retry);
  }
  return response.body(p);
 }
 @ExceptionHandler(ResourceAccessException.class)
 public ProblemDetail timeout(ResourceAccessException e) {return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,"Upstream connection failed");}
 @ExceptionHandler(IllegalArgumentException.class)
 public ProblemDetail badRequest(IllegalArgumentException e) {return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());}
 @ExceptionHandler(IllegalStateException.class)
 public ProblemDetail configuration(IllegalStateException e) {return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,"Integration configuration or upstream response is invalid");}
}
