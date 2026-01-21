package com.back.chat.app;


import com.back.chat.dto.request.ChatMessageReportRequestDto;
import com.back.chat.dto.request.ChatMessageSendRequestDto;
import com.back.chat.dto.response.ChatMessageHistoryResponseDto;
import com.back.chat.dto.response.ChatMessageReportResponseDto;
import com.back.chat.dto.response.ChatRoomEnterResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatFacade {

    private final ChatEnterChatRoomUseCase chatEnterChatRoomUseCase;
    private final ChatSendMessageUseCase chatSendMessageUseCase;
    private final ChatGetHistoryUseCase chatGetHistoryUseCase;
    private final ChatReportMessageUseCase chatReportMessageUseCase;

    @Transactional
    public ChatRoomEnterResponseDto enterChatRoom(Long brandId){
        return chatEnterChatRoomUseCase.enterChatRoom(brandId);
    }

    @Transactional
    public void sendMessage(ChatMessageSendRequestDto requestDto, Long userId) {
        chatSendMessageUseCase.sendMessage(requestDto, userId);
    }

    @Transactional(readOnly = true)
    public ChatMessageHistoryResponseDto getHistory(Long roomId, Long cursorMessageId) {
        return chatGetHistoryUseCase.getHistory(roomId, cursorMessageId);
    }

    @Transactional
    public ChatMessageReportResponseDto reportMessage(Long reporterUserId, ChatMessageReportRequestDto requestDto) {
        return chatReportMessageUseCase.reportMessage(reporterUserId, requestDto);
    }
}
