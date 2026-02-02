package com.back.chat.event;

public record ChatMessageDeletedEvent (
        Long messageId,
        Long roomId
){
}
