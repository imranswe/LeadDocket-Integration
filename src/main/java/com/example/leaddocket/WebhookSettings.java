package com.example.leaddocket;


import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("webhook")
public record WebhookSettings(int maxBodyBytes, int maxAttempts, boolean workerEnabled) {}
