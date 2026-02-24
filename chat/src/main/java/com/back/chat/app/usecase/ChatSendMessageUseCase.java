package com.back.chat.app.usecase;

import com.back.chat.adapter.out.metrics.ChatMetrics;
import com.back.chat.adapter.out.redis.RedisChatRestrictionReader;
import com.back.chat.app.ChatSupport;
import com.back.chat.domain.entity.ChatMessage;
import com.back.chat.adapter.in.web.dto.request.ChatMessageSendRequestDto;
import com.back.chat.adapter.out.redis.payload.ChatMessagePayload;
import com.back.chat.domain.event.ChatMessageSavedEvent;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.ForbiddenException;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatSendMessageUseCase {

    private final ChatSupport chatSupport;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisChatRestrictionReader redisChatRestrictionReader;

    private final ChatMetrics metrics;

    @Transactional
    public void sendMessage(ChatMessageSendRequestDto requestDto, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        Long roomId = requestDto.roomId();

        if(!chatSupport.existsRoomById(roomId)){
            throw new BadRequestException(FailureCode.CHAT_ROOM_NOT_FOUND);
        }

        if (redisChatRestrictionReader.isRestricted(userId)) {
            throw new ForbiddenException(FailureCode.CHAT_RESTRICTED);
        }

        ChatMessage savedMessage = metrics.recordCallable(
                "resello_chat_message_save_seconds",
                Tags.of("result", "ok"),
                () -> chatSupport.saveMessage(
                        ChatMessage.create(roomId, userId, requestDto.content())
                )
        );

        ChatMessagePayload payload = new ChatMessagePayload(
                savedMessage.getId(),
                userId,
                roomId,
                savedMessage.getContent(),
                savedMessage.getMessageStatus(),
                savedMessage.getCreatedAt()
        );

        eventPublisher.publishEvent(new ChatMessageSavedEvent(roomId, payload));
    }

}
