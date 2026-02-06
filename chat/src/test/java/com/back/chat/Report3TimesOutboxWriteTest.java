package com.back.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.back.chat.adapter.out.ChatMessageRepository;
import com.back.chat.adapter.out.ChatRoomRepository;
import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.adapter.out.outbox.OutboxStatus;
import com.back.chat.app.ChatReportMessageUseCase;
import com.back.chat.domain.ChatMessage;
import com.back.chat.domain.ChatReportReason;
import com.back.chat.domain.ChatRoom;
import com.back.chat.dto.request.ChatMessageReportRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class Report3TimesOutboxWriteTest {

    @Autowired
    ChatReportMessageUseCase chatReportMessageUseCase;
    @Autowired
    ChatOutboxRepository chatOutboxRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;           // ✅ 추가
    @Autowired
    ChatMessageRepository chatMessageRepository;
    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void report3Times_shouldWriteOutboxPending() {
        TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        AtomicReference<Long> roomIdRef = new AtomicReference<>();
        AtomicReference<Long> messageIdRef = new AtomicReference<>();

        requiresNew.executeWithoutResult(status -> {
            ChatRoom savedRoom = chatRoomRepository.save(new ChatRoom(1L));
            ChatMessage savedMessage = chatMessageRepository.save(
                    ChatMessage.create(savedRoom.getId(), 999L, "test message")
            );
            chatOutboxRepository.deleteAll();

            roomIdRef.set(savedRoom.getId());
            messageIdRef.set(savedMessage.getId());
        });

        Long roomId = roomIdRef.get();
        Long messageId = messageIdRef.get();

        requiresNew.executeWithoutResult(status -> {
            chatReportMessageUseCase.reportMessage(11L,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.COMMERCIAL_AD));
            chatReportMessageUseCase.reportMessage(12L,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.FISHING_HARASSMENT_SPAM));
            chatReportMessageUseCase.reportMessage(13L,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.INSULT));
        });

        // 🔥 AFTER_COMMIT 리스너가 @Async면 레이스 날 수 있으니 아래 "2)" 참고
        List<ChatOutbox> all = chatOutboxRepository.findAll();

        ChatOutbox event = all.stream()
                .filter(o -> o.getStatus() == OutboxStatus.PENDING)
                .filter(o -> o.getPayload() != null)
                .findFirst()
                .orElseThrow();

        assertThat(event.getPayload())
                .containsPattern("\"roomId\"\\s*:\\s*" + roomId);

        assertThat(event.getPayload())
                .containsPattern("\"messageId\"\\s*:\\s*" + messageId);
    }
}
