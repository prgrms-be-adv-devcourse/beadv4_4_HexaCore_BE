package com.back.chat.adapter.in.web.dto.request;

import com.back.chat.domain.ChatReportReason;
import jakarta.validation.constraints.NotNull;

public record ChatMessageReportRequestDto (
        @NotNull
        Long chatMessageId,
        @NotNull
        ChatReportReason reportReason
){

}
