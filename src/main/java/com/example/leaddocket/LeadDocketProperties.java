package com.example.leaddocket;


import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("leaddocket")
public record LeadDocketProperties(String apiBaseUrl,String apiKey,String apiAuthMode,String apiKeyName,
 String opportunityUrl,String webhookKey,String eventIdField,String deduplicationMode,Map<String,Operation> operations) {
 public record Operation(String method,String path,String contentType){}
}
