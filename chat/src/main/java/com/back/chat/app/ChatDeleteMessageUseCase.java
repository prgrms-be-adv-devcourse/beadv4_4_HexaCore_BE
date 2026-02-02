package com.back.chat.app;

import com.back.chat.domain.ChatMessage;
import com.back.chat.event.ChatMessageDeletedEvent;
import com.back.chat.mapper.ChatMessageMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatDeleteMessageUseCase {
    private final ChatSupport chatSupport;
    private final ApplicationEventPublisher eventPublisher;

    public void deleteMessage(Long userId, Long messageId) {
        ChatMessage message = chatSupport.findMessageById(messageId)
                .orElseThrow(() -> new BadRequestException(FailureCode.CHAT_MESSAGE_NOT_FOUND));

        if (!message.getUserId().equals(userId)) {
            throw new ForbiddenException(FailureCode.CHAT_DELETE_NOT_ALLOWED);
        }

        int updated = chatSupport.deleteIfNotDeleted(messageId);

        ChatMessage latest = chatSupport.findMessageById(messageId)
                .orElseThrow(() -> new BadRequestException(FailureCode.CHAT_MESSAGE_NOT_FOUND));

        if (updated == 1) {
            eventPublisher.publishEvent(new ChatMessageDeletedEvent(
                    latest.getId(),
                    latest.getRoomId()
            ));
        }
    }
}
