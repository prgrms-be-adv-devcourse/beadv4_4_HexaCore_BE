package com.back.chat.app;


import com.back.chat.adapter.out.metrics.ChatMetrics;
import com.back.chat.app.usecase.*;
import com.back.chat.adapter.in.web.dto.request.ChatMessageReportRequestDto;
import com.back.chat.adapter.in.web.dto.request.ChatMessageSendRequestDto;
import com.back.chat.adapter.in.web.dto.response.ChatMessageHistoryResponseDto;
import com.back.chat.adapter.in.web.dto.response.ChatMessageReportResponseDto;
import com.back.chat.adapter.in.web.dto.response.ChatRoomEnterResponseDto;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final ChatEnterChatRoomUseCase chatEnterChatRoomUseCase;
    private final ChatSendMessageUseCase chatSendMessageUseCase;
    private final ChatGetHistoryUseCase chatGetHistoryUseCase;
    private final ChatReportMessageUseCase chatReportMessageUseCase;
    private final ChatDeleteMessageUseCase chatDeleteMessageUseCase;
    private final CreateChatRoomsUseCase createChatRoomsUseCase;
    private final DeleteChatRoomUseCase deleteChatRoomUseCase;

    private final ChatMetrics metrics;

    @Transactional
    public ChatRoomEnterResponseDto enterChatRoom(Long brandId, Long userId){
        return chatEnterChatRoomUseCase.enterChatRoom(brandId, userId);
    }

    @Transactional
    public void sendMessage(ChatMessageSendRequestDto requestDto, Long userId) {
        metrics.incWsSendTotal();

        metrics.recordRunnable(
                "resello_chat_ws_send",
                Tags.of("result", "ok"),
                () -> chatSendMessageUseCase.sendMessage(requestDto, userId)
        );
    }

    @Transactional(readOnly = true)
    public ChatMessageHistoryResponseDto getHistory(Long roomId, Long cursorMessageId) {
        return metrics.recordCallable(
                "resello_chat_history_query",
                Tags.of("result","ok"),
                () -> chatGetHistoryUseCase.getHistory(roomId, cursorMessageId)
        );
    }

    @Transactional
    public ChatMessageReportResponseDto reportMessage(Long reporterUserId, ChatMessageReportRequestDto requestDto) {
        return metrics.recordCallable(
                "resello_chat_report",
                Tags.of("result","ok"),
                () -> {
                    return chatReportMessageUseCase.reportMessage(reporterUserId, requestDto);
                }
        );
    }

    @Transactional
    public void deleteMessage(Long userId, Long messageId) {
        chatDeleteMessageUseCase.deleteMessage(userId, messageId);
    }

    @Transactional
    public void createChatRoomsIfAbsent(List<Long> brandIds) {
        createChatRoomsUseCase.createChatRooms(brandIds);
    }

    @Transactional
    public int deleteChatRoom(Long brandId) {
        return deleteChatRoomUseCase.deleteChatRoom(brandId);
    }
}
