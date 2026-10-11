package com.example.leaddocket;


import com.fasterxml.jackson.databind.JsonNode;
public interface WebhookEventHandler {void handle(String eventId,JsonNode event);}
