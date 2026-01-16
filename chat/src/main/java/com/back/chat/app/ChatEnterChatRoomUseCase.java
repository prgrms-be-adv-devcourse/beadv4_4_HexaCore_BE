package com.back.chat.app;

import com.back.chat.domain.ChatRoom;
import com.back.chat.dto.response.ChatRoomEnterResponseDto;
import com.back.chat.mapper.ChatRoomMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatEnterChatRoomUseCase {
    private final ChatSupport chatSupport;


    public ChatRoomEnterResponseDto enterChatRoom(Long brandId) {
        ChatRoom chatRoom = chatSupport.findRoomByBrandId(brandId).orElseThrow(()-> new BadRequestException(FailureCode.ENTITY_NOT_FOUND));
        return ChatRoomMapper.toEnterResponseDto(chatRoom);
    }
}
