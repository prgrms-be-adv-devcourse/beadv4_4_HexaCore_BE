package com.back.chat.adapter.in.web.mapper;

import com.back.chat.domain.entity.ChatMessage;
import com.back.chat.adapter.in.web.dto.response.ChatMessageHistoryResponseDto;
import com.back.chat.adapter.in.web.dto.response.ChatMessageReportResponseDto;

import java.util.List;

public class ChatMessageMapper {

    public static ChatMessageHistoryResponseDto toHistoryResponseDto(Long roomId, List<ChatMessageHistoryResponseDto.ChatMessageItemDto> itemDtoList, Long nextCursorMessageId, boolean hasNext) {
        return new ChatMessageHistoryResponseDto(
                roomId,
                itemDtoList,
                nextCursorMessageId,
                hasNext
        );
    }

    public static ChatMessageHistoryResponseDto.ChatMessageItemDto toItemDto(ChatMessage m) {
        return new ChatMessageHistoryResponseDto.ChatMessageItemDto(
                m.getId(),
                m.getUserId(),
                m.getContent(),
                m.getMessageStatus(),
                m.getCreatedAt()
        );
    }

    public static ChatMessageReportResponseDto toReportResponseDto (ChatMessage m){
        return new ChatMessageReportResponseDto(
                m.getId(),
                m.getMessageStatus(),
                m.getReportCount()
        );
    }

}
