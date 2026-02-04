package com.back.chat.dto.response;

import com.back.chat.domain.MessageStatus;

public record ChatMessageReportResponseDto (
        Long chatMessageId,
        MessageStatus messageStatus,
        int reportCount
){
}
