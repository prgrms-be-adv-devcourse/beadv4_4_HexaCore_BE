package com.back.chat.dto.response;

public record ChatMessageReportResponseDto (
        Long chatMessageId,
        boolean isBlinded,
        int reportCount
){
}
