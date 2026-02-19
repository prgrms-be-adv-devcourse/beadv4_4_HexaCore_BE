package com.back.chat.app.usecase;

import com.back.chat.app.ChatSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteChatRoomUseCase {

    private final ChatSupport chatSupport;

    public int deleteChatRoom(Long brandId) {
        return chatSupport.deleteChatRoomByBrandId(brandId);
    }
}
