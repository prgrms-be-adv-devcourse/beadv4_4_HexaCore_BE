package com.back.chat.domain.event;


import tools.jackson.databind.JsonNode;

public record ChatEventEnvelope(
        ChatEventType type,
        JsonNode data
) {}
