package com.back.chat.event;

public record ChatMessageBlindedEvent (
        Long chatMessageId,
        Long roomId,
        Long reportedUserId){
}
