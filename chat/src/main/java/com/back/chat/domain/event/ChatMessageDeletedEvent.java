package com.back.chat.domain.event;

public record ChatMessageDeletedEvent (
        Long messageId,
        Long roomId
){
}
