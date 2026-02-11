package com.back.chat.event;


import tools.jackson.databind.JsonNode;

public record ChatEventEnvelope(
        ChatEventType type,
        JsonNode data
) {}
