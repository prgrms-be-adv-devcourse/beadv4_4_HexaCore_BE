package com.back.chat.app.usecase;

import com.back.chat.app.ChatSupport;
import com.back.chat.domain.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateChatRoomsUseCase {

    private final ChatSupport chatSupport;

    public void createChatRooms(List<Long> brandIds) {
        chatSupport.saveChatRooms(brandIds.stream()
                .map(ChatRoom::new)
                .toList()
        );
    }
}
