package com.back.chat.domain.event;

public record ChatMessageBlindedEvent (
        Long chatMessageId,
        Long roomId,
        Long reportedUserId){
}
