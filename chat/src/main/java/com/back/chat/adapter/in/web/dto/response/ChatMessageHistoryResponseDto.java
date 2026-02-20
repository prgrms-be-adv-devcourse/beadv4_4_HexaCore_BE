package com.back.chat.adapter.in.web.dto.response;

import com.back.chat.domain.MessageStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMessageHistoryResponseDto(
        Long roomId,
        List<ChatMessageItemDto> messages,
        Long nextCursorMessageId,
        boolean hasNext
) {
    public record ChatMessageItemDto(
            Long messageId,
            Long userId,
            String content,
            MessageStatus messageStatus,
            LocalDateTime createdAt
    ) {}
}
