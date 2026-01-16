package com.back.chat.adapter.in;

import com.back.chat.app.ChatFacade;
import com.back.chat.dto.response.ChatRoomEnterResponseDto;
import com.back.chat.mapper.ChatRoomMapper;
import com.back.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
