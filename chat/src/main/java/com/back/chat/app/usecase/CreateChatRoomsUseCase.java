package com.back.chat.app.usecase;

import com.back.chat.app.ChatSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateChatRoomsUseCase {

    private final ChatSupport chatSupport;

    public void createChatRooms(List<Long> brandIds) {
        for (Long brandId : brandIds) {
            int inserted = chatSupport.insertIfNotExists(brandId);

            if (inserted == 1) {
                log.info("[CHAT] room created. brandId={}", brandId);
            } else {
                log.info("[CHAT] room already exists. brandId={}", brandId);
            }
        }
    }
}
