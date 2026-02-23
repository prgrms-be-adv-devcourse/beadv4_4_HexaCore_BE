package com.back.chat.adapter.out.redis;

import com.back.chat.domain.event.ChatEventEnvelope;
import com.back.chat.domain.event.ChatEventType;
import com.back.chat.adapter.out.redis.payload.ChatMessageBlindedPayload;
import com.back.chat.adapter.out.redis.payload.ChatMessageDeletedPayload;
import com.back.chat.adapter.out.redis.payload.ChatMessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static com.back.chat.adapter.in.redis.RedisSubscriberConfig.REDIS_PUBSUB_CHAT_ROOM_PATTERN;


@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatEventPublisher {


    private final StringRedisTemplate stringRedisTemplate;
    private final JsonMapper jsonMapper;

    public void publishChatMessage(Long roomId, ChatMessagePayload payload) {
        publish(roomId, ChatEventType.CHAT_MESSAGE, jsonMapper.valueToTree(payload));
    }

    public void publishMessageBlinded(Long roomId, ChatMessageBlindedPayload payload) {
        publish(roomId, ChatEventType.MESSAGE_BLINDED, jsonMapper.valueToTree(payload));
    }

    public void publishMessageDeleted(Long roomId, ChatMessageDeletedPayload payload){
        publish(roomId, ChatEventType.MESSAGE_DELETED, jsonMapper.valueToTree(payload));
    }

    private void publish(Long roomId, ChatEventType type, JsonNode data) {
        String channel = REDIS_PUBSUB_CHAT_ROOM_PATTERN + roomId;
        ChatEventEnvelope envelope = new ChatEventEnvelope(type, data);

        try {
            String message = jsonMapper.writeValueAsString(envelope);
            stringRedisTemplate.convertAndSend(channel, message);
            log.info("[CHAT][REDIS-PUB] channel={}, type={}, roomId={}", channel, type, roomId);
        } catch (Exception e) {
            log.error("[CHAT][REDIS-PUB][ERROR] channel={}, type={}, roomId={}", channel, type, roomId, e);
        }
    }
}
