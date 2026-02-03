package com.back.detector.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorPolicyTest {

    @Test
    @DisplayName("BID_SPAM 정책의 maxAttempts 값이 5이어야 한다")
    void bid_spam_max_attempts() {
        assertThat(DetectorPolicy.BID_SPAM.getMaxAttempts()).isEqualTo(5);
    }

    @Test
    @DisplayName("BID_SPAM 정책의 timeWindowMinutes 값이 1이어야 한다")
    void bid_spam_time_window() {
        assertThat(DetectorPolicy.BID_SPAM.getTimeWindowMinutes()).isEqualTo(1);
    }

    @Test
    @DisplayName("BID_SPAM 정책의 banReason 값이 올바르지 확인한다")
    void bid_spam_ban_reason() {
        assertThat(DetectorPolicy.BID_SPAM.getBanReason()).isEqualTo("반복적인 입찰 시도");
    }
}
