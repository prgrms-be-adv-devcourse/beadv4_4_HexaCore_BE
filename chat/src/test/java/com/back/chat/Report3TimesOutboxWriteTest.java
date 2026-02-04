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
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class Report3TimesOutboxWriteTest {

    @Autowired
    ChatReportMessageUseCase chatReportMessageUseCase;
    @Autowired
    ChatOutboxRepository chatOutboxRepository;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired
    ChatRoomRepository chatRoomRepository;           // ✅ 추가
    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Test
    void report3Times_shouldWriteOutboxPending() {
        // given: 테스트용 roomId/messageId를 트랜잭션에서 생성 후 밖으로 꺼내기
        AtomicReference<Long> roomIdRef = new AtomicReference<>();
        AtomicReference<Long> messageIdRef = new AtomicReference<>();

        transactionTemplate.executeWithoutResult(status -> {
            ChatRoom savedRoom = chatRoomRepository.save(
                    new ChatRoom(1L)
            );

            ChatMessage savedMessage = chatMessageRepository.save(
                     ChatMessage.create(
                            savedRoom.getId(),
                            999L,
                            "test message"
                    )
            );

            // outbox 테스트 간섭 방지
            chatOutboxRepository.deleteAll();

            roomIdRef.set(savedRoom.getId());
            messageIdRef.set(savedMessage.getId());
        });

        Long roomId = roomIdRef.get();
        Long messageId = messageIdRef.get();

        Long reporter1 = 11L;
        Long reporter2 = 12L;
        Long reporter3 = 13L;

        // when: 신고 3회 (커밋되게 transactionTemplate 사용)
        transactionTemplate.executeWithoutResult(status -> {
            chatReportMessageUseCase.reportMessage(reporter1,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.COMMERCIAL_AD));

            chatReportMessageUseCase.reportMessage(reporter2,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.FISHING_HARASSMENT_SPAM));

            chatReportMessageUseCase.reportMessage(reporter3,
                    new ChatMessageReportRequestDto(messageId, ChatReportReason.INSULT));
        });

        // then: outbox 저장 확인
        ChatOutbox event = chatOutboxRepository.findAll().stream()
                .filter(o -> o.getStatus() == OutboxStatus.PENDING)
                .filter(o -> o.getPayload() != null)
                .filter(o -> o.getPayload().contains("\"messageId\":" + messageId))
                .findFirst()
                .orElseThrow();

        // 너 이벤트 타입명/enum에 맞게
        assertThat(event.getEventType()).isIn("MESSAGE_BLINDED", "REPORT_THRESHOLD_REACHED");
        assertThat(event.getPayload()).contains("\"roomId\":" + roomId);
        assertThat(event.getPayload()).contains("\"messageId\":" + messageId);
    }
}
