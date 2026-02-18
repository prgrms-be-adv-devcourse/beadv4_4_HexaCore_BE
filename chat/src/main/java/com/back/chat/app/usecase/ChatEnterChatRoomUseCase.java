package com.back.chat.app.usecase;

import com.back.chat.app.ChatSupport;
import com.back.chat.domain.entity.ChatRoom;
import com.back.chat.adapter.in.web.dto.response.ChatRoomEnterResponseDto;
import com.back.chat.adapter.in.web.mapper.ChatRoomMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatEnterChatRoomUseCase {
    private final ChatSupport chatSupport;


    public ChatRoomEnterResponseDto enterChatRoom(Long brandId, Long userId) {
        ChatRoom chatRoom = chatSupport.findRoomByBrandId(brandId).orElseThrow(()-> new BadRequestException(FailureCode.CHAT_ROOM_NOT_FOUND));
        return ChatRoomMapper.toEnterResponseDto(chatRoom, userId);
    }
}
