package com.back.chat.adapter.in;

import com.back.chat.app.ChatFacade;
import com.back.chat.dto.request.ChatMessageReportRequestDto;
import com.back.chat.dto.response.ChatMessageHistoryResponseDto;
import com.back.chat.dto.response.ChatMessageReportResponseDto;
import com.back.chat.dto.response.ChatRoomEnterResponseDto;
import com.back.common.response.CommonResponse;
import com.back.security.principal.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;

    @PostMapping("/enter")
    public CommonResponse<ChatRoomEnterResponseDto> enter(@RequestParam("brandId") Long brandId){
        return CommonResponse.success(
                        HttpStatus.OK,
                "채팅방 입장 성공",
                chatFacade.enterChatRoom(brandId)
        );
    }

    @GetMapping("/history")
    public CommonResponse<ChatMessageHistoryResponseDto> getHistory(@RequestParam("roomId") Long roomID, @RequestParam(value = "cursorMessageId", required = false) Long cursorMessageId){
        return CommonResponse.success(
                HttpStatus.OK,
                "채팅 메시지 히스토리 조회 성공",
                chatFacade.getHistory(roomID,cursorMessageId)
        );
    }

    @PostMapping("/report")
    public CommonResponse<ChatMessageReportResponseDto> reportMessage(@AuthenticationPrincipal AuthPrincipal authPrincipal, @Valid @RequestBody ChatMessageReportRequestDto requestDto){
        return CommonResponse.success(
                HttpStatus.OK,
                "채팅 메시지 신고 성공",
                chatFacade.reportMessage(authPrincipal.getUserId(),requestDto)
        );
    }
}
