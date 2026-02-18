package com.back.chat.adapter.in.web.dto.response;

import com.back.chat.domain.MessageStatus;

public record ChatMessageReportResponseDto (
        Long chatMessageId,
        MessageStatus messageStatus,
        int reportCount
){
}
