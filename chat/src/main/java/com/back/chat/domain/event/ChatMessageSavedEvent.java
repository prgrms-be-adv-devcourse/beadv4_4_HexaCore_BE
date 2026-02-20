package com.back.chat.domain.event;


import com.back.chat.adapter.out.redis.payload.ChatMessagePayload;

public record ChatMessageSavedEvent(
        Long roomId,
        ChatMessagePayload payload
) {}
