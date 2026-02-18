package com.back.chat.adapter.in.web.mapper;

import com.back.chat.domain.entity.ChatRoom;
import com.back.chat.adapter.in.web.dto.response.ChatRoomEnterResponseDto;

public class ChatRoomMapper {
    private static final String CHAT_ROOM_TOPIC_PREFIX = "/topic/chat/room/";

    public static ChatRoomEnterResponseDto toEnterResponseDto(ChatRoom chatRoom, Long userId) {
        return new ChatRoomEnterResponseDto(chatRoom.getId(), CHAT_ROOM_TOPIC_PREFIX + chatRoom.getId(),userId);
    }
}
