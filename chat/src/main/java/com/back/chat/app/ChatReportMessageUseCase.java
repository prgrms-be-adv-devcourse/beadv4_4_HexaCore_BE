package com.back.chat.app;

import com.back.chat.domain.ChatMessage;
import com.back.chat.domain.ChatMessageBlindPolicy;
import com.back.chat.domain.ChatReport;
import com.back.chat.dto.request.ChatMessageReportRequestDto;
import com.back.chat.dto.response.ChatMessageReportResponseDto;
import com.back.chat.event.ChatMessageBlindedEvent;
import com.back.chat.mapper.ChatMessageMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatReportMessageUseCase {
    
    private final ChatSupport chatSupport;
    private final ApplicationEventPublisher eventPublisher;

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
        boolean blindedNow =
                chatSupport.blindIfReached(messageId, ChatMessageBlindPolicy.MESSAGE_BLIND_THRESHOLD) == 1;

        // 최신 상태 조회
        ChatMessage latestMessage = chatSupport.findMessageById(requestDto.chatMessageId())
                .orElseThrow(() -> new BadRequestException(FailureCode.CHAT_MESSAGE_NOT_FOUND));

        if (blindedNow) {
            eventPublisher.publishEvent(new ChatMessageBlindedEvent(
                    latestMessage.getId(),
                    latestMessage.getRoomId(),
                    latestMessage.getUserId()
            ));
        }

        return ChatMessageMapper.toReportResponseDto(latestMessage);
    }
}
