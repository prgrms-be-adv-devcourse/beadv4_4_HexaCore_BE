package com.back.chat.adapter.out.redis;

import com.back.chat.event.ChatEventEnvelope;
import com.back.chat.event.ChatEventType;
import com.back.chat.event.payload.ChatMessageBlindedPayload;
import com.back.chat.event.payload.ChatMessagePayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatEventPublisher {

    private static final String CHANNEL_PREFIX = "chatroom:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publishChatMessage(Long roomId, ChatMessagePayload payload) {
        publish(roomId, ChatEventType.CHAT_MESSAGE, objectMapper.valueToTree(payload));
    }

    public void publishMessageBlinded(Long roomId, ChatMessageBlindedPayload payload) {
        publish(roomId, ChatEventType.MESSAGE_BLINDED, objectMapper.valueToTree(payload));
    }

    private void publish(Long roomId, ChatEventType type, JsonNode data) {
        String channel = CHANNEL_PREFIX + roomId;
        ChatEventEnvelope envelope = new ChatEventEnvelope(type, data);

        try {
            String message = objectMapper.writeValueAsString(envelope);
            stringRedisTemplate.convertAndSend(channel, message);
            log.info("[CHAT][REDIS-PUB] channel={}, type={}, roomId={}", channel, type, roomId);
        } catch (Exception e) {
            log.error("[CHAT][REDIS-PUB][ERROR] channel={}, type={}, roomId={}", channel, type, roomId, e);
        }
    }
}
