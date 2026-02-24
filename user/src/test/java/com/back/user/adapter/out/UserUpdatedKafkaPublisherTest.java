package com.back.user.adapter.out;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.user.domain.event.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserUpdatedKafkaPublisherTest {

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private UserUpdatedKafkaPublisher userUpdatedKafkaPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userUpdatedKafkaPublisher, "userUpdatedTopic", "user.account.updated");
    }

    @Test
    @DisplayName("회원 정보 수정 이벤트 수신 시 올바른 토픽으로 Envelope를 발행한다")
    void publishUserUpdated_success_publishesToCorrectTopic() {
        // given
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .id(1L)
                .nickname("newNick")
                .name("김철수")
                .email("kim@example.com")
                .address("서울시 서초구")
                .phone("01098765432")
                .build();

        // when
        userUpdatedKafkaPublisher.publishUserUpdated(event);

        // then
        verify(kafkaEventPublisher).publish(eq("user.account.updated"), any(Envelope.class));
    }
}
