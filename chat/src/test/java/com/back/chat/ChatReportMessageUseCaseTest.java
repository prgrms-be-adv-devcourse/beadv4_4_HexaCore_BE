package com.back.chat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.back.chat.adapter.out.ChatMessageRepository;
import com.back.chat.adapter.out.ChatReportRepository;
import com.back.chat.app.ChatReportMessageUseCase;
import com.back.chat.domain.ChatMessage;
import com.back.chat.domain.ChatReportReason;
import com.back.chat.dto.request.ChatMessageReportRequestDto;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = ChatApplication.class)
class ChatReportMessageUseCaseTest {

    @Autowired
    private ChatReportMessageUseCase chatReportMessageUseCase;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatReportRepository chatReportRepository;

    private Long messageId;
    private Long roomId = 1L;
    private Long authorUserId = 10L;

    @BeforeEach
    void setUp() {
        chatReportRepository.deleteAll();
        chatMessageRepository.deleteAll();

        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.create(roomId, authorUserId, "hello")
        );
        messageId = saved.getId();
    }

    @Test
    @Transactional
    void report_should_increment_reportCount_and_blind_on_third_report() {
        // given
        Long reporter1 = 101L;
        Long reporter2 = 102L;
        Long reporter3 = 103L;

        ChatMessageReportRequestDto dto = new ChatMessageReportRequestDto(
                messageId,
                ChatReportReason.FISHING_HARASSMENT_SPAM
        );

        // when: 1st report
        chatReportMessageUseCase.reportMessage(reporter1, dto);
        // when: 2nd report
        chatReportMessageUseCase.reportMessage(reporter2, dto);

        ChatMessage after2 = chatMessageRepository.findById(messageId).orElseThrow();
        assertThat(after2.getReportCount()).isEqualTo(2);
        assertThat(after2.isBlinded()).isFalse();

        // when: 3rd report -> should blind
        chatReportMessageUseCase.reportMessage(reporter3, dto);

        // then
        ChatMessage after3 = chatMessageRepository.findById(messageId).orElseThrow();
        assertThat(after3.getReportCount()).isEqualTo(3);
        assertThat(after3.isBlinded()).isTrue();
    }

    @Test
    @Transactional
    void report_should_reject_duplicate_report_by_same_user() {
        // given
        Long reporter = 201L;
        ChatMessageReportRequestDto dto = new ChatMessageReportRequestDto(
                messageId,
                ChatReportReason.FISHING_HARASSMENT_SPAM
        );

        // when
        chatReportMessageUseCase.reportMessage(reporter, dto);

        // then: 두 번째 신고는 막혀야 함
        assertThatThrownBy(() -> chatReportMessageUseCase.reportMessage(reporter, dto))
                .isInstanceOf(ConflictException.class);
    }

}
