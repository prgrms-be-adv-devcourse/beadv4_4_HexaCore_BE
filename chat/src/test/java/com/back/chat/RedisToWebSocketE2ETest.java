package com.back.chat;

/*
import com.back.chat.adapter.out.redis.RedisChatMessageSubscriber;
import com.back.chat.event.payload.ChatMessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = RedisToWebSocketE2ETest.TestApp.class,
        properties = {
                "springdoc.swagger-ui.enabled=false",
                "springdoc.api-docs.enabled=false"
        }
)
@Import({
        RedisToWebSocketE2ETest.TestWsConfig.class,
        RedisToWebSocketE2ETest.WsPermitAllSecurityConfig.class
})
class RedisToWebSocketE2ETest {

    @SpringBootApplication(scanBasePackages = "com.back")
    static class TestApp {}

    @TestConfiguration
    @EnableWebSocketMessageBroker
    static class TestWsConfig implements WebSocketMessageBrokerConfigurer {
        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws/chat-test").setAllowedOriginPatterns("*");
        }
        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic");
            registry.setApplicationDestinationPrefixes("/app");
        }
    }

    @TestConfiguration
    static class WsPermitAllSecurityConfig {
        @Bean
        @Order(0)
        SecurityFilterChain wsPermitAll(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher("/ws/**")
                    .csrf(csrf -> csrf.disable())
                    .cors(Customizer.withDefaults())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @LocalServerPort int port;

    @Autowired StringRedisTemplate redis;
    @Autowired RedisConnectionFactory redisConnectionFactory;

    @Autowired RedisChatMessageSubscriber subscriber;

    private WebSocketStompClient stomp;
    private ObjectMapper om;

    @BeforeEach
    @SuppressWarnings({"deprecation", "removal"})
    void setUp() {
        stomp = new WebSocketStompClient(new StandardWebSocketClient());

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("stomp-test-");
        scheduler.initialize();
        stomp.setTaskScheduler(scheduler);

        om = JsonMapper.builder().findAndAddModules().build();
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(om);
        stomp.setMessageConverter(converter);
    }

    @AfterEach
    void tearDown() {
        if (stomp != null) stomp.stop();
    }

    @Test
    void redis_publish_then_ws_should_receive() throws Exception {
        long roomId = 1L;

        String redisChannel = "chatroom:" + roomId;
        String wsTopic = "/topic/chat/room/" + roomId;

        // subscriber를 Redis에 직접 등록.
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(redisChannel));
        container.afterPropertiesSet();
        container.start();

        CompletableFuture<ChatMessagePayload> wsReceived = new CompletableFuture<>();
        CountDownLatch connected = new CountDownLatch(1);

        StompSession session = stomp.connectAsync(
                "ws://localhost:" + port + "/ws/chat-test",
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        connected.countDown();
                    }
                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        wsReceived.completeExceptionally(exception);
                    }
                }
        ).get(3, TimeUnit.SECONDS);

        assertThat(connected.await(2, TimeUnit.SECONDS)).isTrue();

        session.subscribe(wsTopic, new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return ChatMessagePayload.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) {
                wsReceived.complete((ChatMessagePayload) payload);
            }
        });

        Thread.sleep(200);

        // Redis publish
        ChatMessagePayload out = new ChatMessagePayload(
                1L, roomId, 1L, "hello", false, LocalDateTime.now()
        );
        redis.convertAndSend(redisChannel, om.writeValueAsString(out));

        // WS 수신 검증
        ChatMessagePayload in = wsReceived.get(5, TimeUnit.SECONDS);
        assertThat(in.roomId()).isEqualTo(roomId);

        session.disconnect();
        container.stop();
    }
}

 */
