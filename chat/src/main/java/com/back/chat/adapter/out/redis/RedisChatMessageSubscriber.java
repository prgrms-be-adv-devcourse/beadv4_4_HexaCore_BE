package com.back.chat.adapter.out.redis;

import com.back.chat.event.ChatEventEnvelope;
import com.back.chat.event.payload.ChatMessageBlindedPayload;
import com.back.chat.event.payload.ChatMessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageSubscriber implements MessageListener {

    private static final String ROOM_TOPIC_PREFIX = "/topic/chat/room/";

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String rawBody = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            ChatEventEnvelope envelope = objectMapper.readValue(rawBody, ChatEventEnvelope.class);

            switch (envelope.type()) {
                case CHAT_MESSAGE -> {
                    ChatMessagePayload payload =
                            objectMapper.treeToValue(envelope.data(), ChatMessagePayload.class);

                    String destination = roomTopic(payload.roomId());
                    messagingTemplate.convertAndSend(destination, payload);

                    log.info("[CHAT][REDIS-SUB] channel={}, type=CHAT_MESSAGE roomId={}, messageId={}, blinded={}, destination={}",
                            channel, payload.roomId(), payload.messageId(), payload.isBlinded(), destination);
                }

                case MESSAGE_BLINDED -> {
                    ChatMessageBlindedPayload payload =
                            objectMapper.treeToValue(envelope.data(), ChatMessageBlindedPayload.class);

                    String destination = roomTopic(payload.roomId());
                    messagingTemplate.convertAndSend(destination, payload);

                    log.info("[CHAT][REDIS-SUB] channel={}, type=MESSAGE_BLINDED roomId={}, messageId={}, destination={}",
                            channel, payload.roomId(), payload.chatMessageId(), destination);
                }

                default -> log.warn("[CHAT][REDIS-SUB][WARN] unknown type. channel={}, rawBody={}", channel, rawBody);
            }

        } catch (Exception e) {
            log.error("[CHAT][REDIS-SUB][ERROR] channel={}, rawBody={}", channel, rawBody, e);
        }
    }

    private String roomTopic(Long roomId) {
        return ROOM_TOPIC_PREFIX + roomId;
    }
}
