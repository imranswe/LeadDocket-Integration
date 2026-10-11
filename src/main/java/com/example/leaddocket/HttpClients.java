package com.example.leaddocket;


import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
final class HttpClients {
 static RestClient create() {
  var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  var factory=new JdkClientHttpRequestFactory(http);factory.setReadTimeout(Duration.ofSeconds(30));
  return RestClient.builder().requestFactory(factory).defaultHeader("User-Agent","SpringIntegration/1.0").build();
 }
 private HttpClients() {}
}
