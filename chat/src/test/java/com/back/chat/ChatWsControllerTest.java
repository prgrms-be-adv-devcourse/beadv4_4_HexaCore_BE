package com.back.chat;

import com.back.chat.adapter.in.ws.ChatWsController;
import com.back.chat.app.ChatFacade;
import com.back.chat.adapter.in.web.dto.request.ChatMessageSendRequestDto;
import com.back.security.principal.AuthPrincipal;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class ChatWsControllerTest {

    @Test
    void sendMessage_shouldCallFacade_withUserIdFromAuthentication() {
        // given
        ChatFacade chatFacade = mock(ChatFacade.class);
        ChatWsController controller = new ChatWsController(chatFacade);

        ChatMessageSendRequestDto dto =
                new ChatMessageSendRequestDto(1L, "hello");

        AuthPrincipal authPrincipal = new AuthPrincipal(10L, "USER");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(authPrincipal, null);

        // when
        controller.sendMessage(dto, authentication);

        // then
        verify(chatFacade, times(1))
                .sendMessage(dto, 10L);
    }
}
