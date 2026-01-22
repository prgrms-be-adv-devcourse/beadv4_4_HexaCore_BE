package com.back.chat;

import com.back.chat.adapter.out.UserClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = UserClientTest.TestApp.class)
@ImportAutoConfiguration(FeignAutoConfiguration.class)
@EnableFeignClients(clients = UserClient.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "user-service.url=http://localhost:8089",
        "spring.main.allow-bean-definition-overriding=true"
})
class UserClientTest {

    static WireMockServer wireMockServer;

    @Autowired
    private UserClient userClient;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void incrementBlindCount_정상호출된다() {
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/internal/users/1/blind-count:increment"))
                        .withQueryParam("messageId", WireMock.equalTo("100"))
                        .willReturn(WireMock.aResponse().withStatus(200))
        );

        assertDoesNotThrow(() -> userClient.incrementBlindCount(1L, 100L));
    }

    @Test
    void getChatRestrictedUntil_정상적으로_시간을_반환한다() {
        WireMock.stubFor(
                WireMock.get(WireMock.urlEqualTo("/internal/users/1/chat-restricted"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("\"2026-01-25T12:00:00\""))
        );

        LocalDateTime result = userClient.getChatRestrictedUntil(1L);

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 1, 25, 12, 0));
    }

    @TestConfiguration
    static class TestApp { }
}


