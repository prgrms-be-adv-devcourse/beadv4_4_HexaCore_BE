package com.back.chat.event;

import com.fasterxml.jackson.databind.JsonNode;

public record ChatEventEnvelope(
        ChatEventType type,
        JsonNode data
) {}
