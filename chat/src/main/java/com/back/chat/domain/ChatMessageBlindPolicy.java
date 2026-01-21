package com.back.chat.domain;

import com.back.chat.event.payload.ChatMessagePayload;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageBlindPolicy {

    private ChatMessageBlindPolicy() {
    }

    public static final int MESSAGE_BLIND_THRESHOLD = 3;

    public static final int CHAT_RESTRICT_THRESHOLD = 3;

}
