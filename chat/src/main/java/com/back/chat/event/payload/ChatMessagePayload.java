package com.back.chat.event.payload;

import com.back.chat.domain.MessageStatus;

import java.time.LocalDateTime;

public record ChatMessagePayload(
        Long messageId,
        Long userId,
        Long roomId,
        String content,
        MessageStatus messageStatus,
        LocalDateTime createdAt
){
}
