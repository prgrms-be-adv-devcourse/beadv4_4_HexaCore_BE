package com.back.chat.adapter.out.redis.payload;

public record ChatMessageDeletedPayload (
        Long roomId,
        Long messageId
){
}
