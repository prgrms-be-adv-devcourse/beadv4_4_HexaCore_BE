package com.back.chat.app.usecase;

import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.app.ChatSupport;
import com.back.chat.domain.entity.ChatMessage;
import com.back.chat.domain.ChatMessageBlindPolicy;
import com.back.chat.domain.entity.ChatReport;
import com.back.chat.adapter.in.web.dto.request.ChatMessageReportRequestDto;
import com.back.chat.adapter.in.web.dto.response.ChatMessageReportResponseDto;
import com.back.chat.domain.event.ChatEventType;
import com.back.chat.domain.event.ChatMessageBlindedEvent;
import com.back.chat.domain.event.ChatOutboxSavedEvent;
import com.back.chat.adapter.in.web.mapper.ChatMessageMapper;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatReportMessageUseCase {

    private final ChatSupport chatSupport;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatOutboxRepository chatOutboxRepository;
    private final JsonMapper jsonMapper;

    public ChatMessageReportResponseDto reportMessage(Long reporterUserId, ChatMessageReportRequestDto requestDto) {
        Long messageId = requestDto.chatMessageId();

        ChatMessage message = chatSupport.findMessageById(messageId)
                .orElseThrow(() -> new BadRequestException(FailureCode.CHAT_MESSAGE_NOT_FOUND));

        try {
            chatSupport.saveReport(ChatReport.create(
                    message,
                    reporterUserId,
                    message.getUserId(),
                    requestDto.reportReason()
            ));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException(FailureCode.REPORT_DUPLICATE);
        }

        int inc = chatSupport.incrementReportCount(messageId);

        if (inc != 1) {
            throw new IllegalStateException(
                    "reportCount 원자적 증가 연산 실패. messageId=" + messageId
            );
        }

        // 이번 신고로 신고 횟수 3회에 도달했는지.
        boolean blindedNow = chatSupport.blindIfReached(messageId, ChatMessageBlindPolicy.MESSAGE_BLIND_THRESHOLD) == 1;

        if (blindedNow) {

            eventPublisher.publishEvent(
                    new ChatMessageBlindedEvent(
                            message.getId(),
                            message.getRoomId(),
                            message.getUserId()
                    )
            );
        }
        LocalDateTime now = LocalDateTime.now();
        String eventId = UUID.randomUUID().toString();

        ChatMessageBlindedKafkaEvent payload =
                new ChatMessageBlindedKafkaEvent(
                        eventId,
                        message.getUserId(),
                        now
                );

            String payloadJson;
            try {
                payloadJson = jsonMapper.writeValueAsString(payload);
            } catch (Exception e) {
                throw new IllegalStateException("Outbox payload 직렬화 실패", e);
            }

                ChatOutbox outbox = chatOutboxRepository.save(
                    ChatOutbox.pending(
                            UUID.fromString(payload.eventId()),
                            "CHAT_MESSAGE",
                            message.getId(),
                            ChatEventType.MESSAGE_BLINDED,
                            payloadJson,
                            now
                    )
            );
                eventPublisher.publishEvent(new ChatOutboxSavedEvent(outbox.getId()));

        return ChatMessageMapper.toReportResponseDto(message);
    }
}
