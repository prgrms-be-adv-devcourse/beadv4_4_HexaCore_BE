package com.back.user.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.user.domain.User;
import com.back.user.domain.enums.Provider;
import com.back.user.domain.enums.Role;
import com.back.user.domain.event.UserUpdatedEvent;
import com.back.user.dto.request.UpdateUserProfileRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUpdateUseCaseTest {

    @InjectMocks
    private UserUpdateUseCase userUpdateUseCase;

    @Mock
    private UserSupport userSupport;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("프로필 수정 성공 시 UserUpdatedEvent가 변경된 값으로 발행된다")
    void updateUserProfile_success_publishesEvent() {
        // given
        User user = User.builder()
                .id(1L)
                .nickname("oldNick")
                .name("홍길동")
                .email("hong@example.com")
                .address("서울시 강남구")
                .phone("01012345678")
                .provider(Provider.KAKAO)
                .providerId("kakao-123")
                .role(Role.USER)
                .build();

        UpdateUserProfileRequestDto request = new UpdateUserProfileRequestDto(
                "newNick", "김철수", "01098765432", "서울시 서초구"
        );

        // when
        userUpdateUseCase.updateUserProfile(user, request);

        // then
        ArgumentCaptor<UserUpdatedEvent> captor = ArgumentCaptor.forClass(UserUpdatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UserUpdatedEvent event = captor.getValue();
        assertThat(event.id()).isEqualTo(1L);
        assertThat(event.nickname()).isEqualTo("newNick");
        assertThat(event.name()).isEqualTo("김철수");
        assertThat(event.email()).isEqualTo("hong@example.com");
        assertThat(event.address()).isEqualTo("서울시 서초구");
        assertThat(event.phone()).isEqualTo("01098765432");
    }

    @Test
    @DisplayName("nickname이 null이면 나머지 필드만 수정되고 이벤트가 발행된다")
    void updateUserProfile_withNullNickname_updatesOtherFieldsAndPublishesEvent() {
        // given
        User user = User.builder()
                .id(2L)
                .nickname("unchanged")
                .name("홍길동")
                .email("hong@example.com")
                .address("서울시 강남구")
                .phone("01012345678")
                .provider(Provider.KAKAO)
                .providerId("kakao-456")
                .role(Role.USER)
                .build();

        UpdateUserProfileRequestDto request = new UpdateUserProfileRequestDto(
                null, null, null, "경기도 성남시"
        );

        // when
        userUpdateUseCase.updateUserProfile(user, request);

        // then
        ArgumentCaptor<UserUpdatedEvent> captor = ArgumentCaptor.forClass(UserUpdatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UserUpdatedEvent event = captor.getValue();
        assertThat(event.nickname()).isNull();
        assertThat(event.address()).isEqualTo("경기도 성남시");
        assertThat(event.email()).isEqualTo("hong@example.com");
    }

    @Test
    @DisplayName("닉네임 중복 시 예외가 발생하고 이벤트는 발행되지 않는다")
    void updateUserProfile_duplicateNickname_throwsAndNoEvent() {
        // given
        User user = User.builder()
                .id(1L)
                .nickname("oldNick")
                .name("홍길동")
                .email("hong@example.com")
                .address("서울시 강남구")
                .phone("01012345678")
                .provider(Provider.KAKAO)
                .providerId("kakao-123")
                .role(Role.USER)
                .build();

        UpdateUserProfileRequestDto request = new UpdateUserProfileRequestDto(
                "duplicateNick", null, null, null
        );

        doThrow(new CustomException(FailureCode.NICKNAME_ALREADY_EXISTS))
                .when(userSupport).validateNicknameAvailable("duplicateNick", "oldNick");

        // when & then
        assertThatThrownBy(() -> userUpdateUseCase.updateUserProfile(user, request))
                .isInstanceOf(CustomException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }
}
