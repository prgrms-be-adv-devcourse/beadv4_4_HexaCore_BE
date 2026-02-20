package com.back.chat.app.usecase;

import com.back.chat.app.ChatSupport;
import com.back.chat.domain.entity.ChatMessage;
import com.back.chat.adapter.in.web.dto.response.ChatMessageHistoryResponseDto;
import com.back.chat.adapter.in.web.mapper.ChatMessageMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatGetHistoryUseCase {
    private final ChatSupport chatSupport;
    private static final int PAGE_SIZE = 30;

    public ChatMessageHistoryResponseDto getHistory(Long roomId, Long cursorMessageId){
        if(!chatSupport.existsRoomById(roomId)){
            throw new BadRequestException(FailureCode.CHAT_ROOM_NOT_FOUND);
        }

        // hasNext 판별 위해 +1
        Pageable pageable = PageRequest.of(0, PAGE_SIZE + 1);


        List<ChatMessage> fetched;
        if (cursorMessageId == null) {
            fetched = chatSupport.findByRoomIdOrderByIdDesc(roomId, pageable);
        } else {
            fetched = chatSupport.findByRoomIdAndIdLessThanOrderByIdDesc(
                    roomId,
                    cursorMessageId,
                    pageable
            );
        }

        // hasNext 판단
        boolean hasNext = fetched.size() == PAGE_SIZE + 1;

        // 실제 내려줄 데이터는 -1
        List<ChatMessage> page = hasNext ? fetched.subList(0, PAGE_SIZE) : fetched;

        // nextCursor 계산 (DESC 기준 마지막이 가장 과거)
        Long nextCursorMessageId = page.isEmpty()
                ? null
                : page.get(page.size() - 1).getId();

        List<ChatMessageHistoryResponseDto.ChatMessageItemDto> itemDtoList = page.stream()
                .map(ChatMessageMapper::toItemDto)
                .toList();

        return ChatMessageMapper.toHistoryResponseDto(
                roomId,
                itemDtoList,
                nextCursorMessageId,
                hasNext
        );
    }
}
