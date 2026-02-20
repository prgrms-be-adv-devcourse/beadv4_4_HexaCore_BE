package com.back.chat.adapter.in.redis;

import com.back.chat.domain.event.ChatEventEnvelope;
import com.back.chat.adapter.out.redis.payload.ChatMessageBlindedPayload;
import com.back.chat.adapter.out.redis.payload.ChatMessageDeletedPayload;
import com.back.chat.adapter.out.redis.payload.ChatMessagePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static com.back.chat.adapter.in.redis.RedisSubscriberConfig.WS_CHAT_ROOM_TOPIC_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMessageSubscriber implements MessageListener {

    private final JsonMapper jsonMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String rawBody = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            ChatEventEnvelope envelope = jsonMapper.readValue(rawBody, ChatEventEnvelope.class);

            switch (envelope.type()) {
                case CHAT_MESSAGE -> {
                    ChatMessagePayload payload =
                            jsonMapper.treeToValue(envelope.data(), ChatMessagePayload.class);

                    String destination = roomTopic(payload.roomId());
                    messagingTemplate.convertAndSend(destination, payload);

                    log.info("[CHAT][REDIS-SUB] channel={}, type=CHAT_MESSAGE roomId={}, messageId={}, messageStatus={}, destination={}",
                            channel, payload.roomId(), payload.messageId(), payload.messageStatus(), destination);
                }

                case MESSAGE_BLINDED -> {
                    ChatMessageBlindedPayload payload =
                            jsonMapper.treeToValue(envelope.data(), ChatMessageBlindedPayload.class);

                    String destination = roomTopic(payload.roomId());
                    messagingTemplate.convertAndSend(destination, payload);

                    log.info("[CHAT][REDIS-SUB] channel={}, type=MESSAGE_BLINDED roomId={}, messageId={}, destination={}",
                            channel, payload.roomId(), payload.chatMessageId(), destination);
                }

                case MESSAGE_DELETED -> {
                    ChatMessageDeletedPayload payload =
                            jsonMapper.treeToValue(envelope.data(), ChatMessageDeletedPayload.class);

                    String destination = roomTopic(payload.roomId());
                    messagingTemplate.convertAndSend(destination,payload);
                }

                default -> log.warn("[CHAT][REDIS-SUB][WARN] unknown type. channel={}, rawBody={}", channel, rawBody);
            }

        } catch (Exception e) {
            log.error("[CHAT][REDIS-SUB][ERROR] channel={}, rawBody={}", channel, rawBody, e);
        }
    }

    private String roomTopic(Long roomId) {
        return WS_CHAT_ROOM_TOPIC_PREFIX + roomId;
    }
}
