package com.back.chat.event.payload;

public record ChatMessageDeletedPayload (
        Long roomId,
        Long messageId
){
}
