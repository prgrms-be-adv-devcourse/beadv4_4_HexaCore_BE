package com.back.chat.adapter.in.ws;

import com.back.chat.app.ChatFacade;
import com.back.chat.adapter.in.web.dto.request.ChatMessageSendRequestDto;
import com.back.security.principal.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatFacade chatFacade;

    @MessageMapping("/message")
    public void sendMessage(
            @Valid @Payload ChatMessageSendRequestDto requestDto,
            Authentication auth
    ) {
        AuthPrincipal authPrincipal = (AuthPrincipal) auth.getPrincipal();

        chatFacade.sendMessage(requestDto, authPrincipal.getUserId());
    }
}
