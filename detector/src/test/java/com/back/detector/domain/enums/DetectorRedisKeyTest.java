package com.back.detector.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorRedisKeyTest {

    @Test
    @DisplayName("BID_COUNT 키는 'bid:count:{userId}' 형식이어야 한다")
    void bid_count_key_format() {
        assertThat(DetectorRedisKey.BID_COUNT.getKey(1L)).isEqualTo("bid:count:1");
        assertThat(DetectorRedisKey.BID_COUNT.getKey(999L)).isEqualTo("bid:count:999");
    }

    @Test
    @DisplayName("VIEW_COUNT 키는 'view:count:{userId}' 형식이어야 한다")
    void view_count_key_format() {
        assertThat(DetectorRedisKey.VIEW_COUNT.getKey(42L)).isEqualTo("view:count:42");
    }

    @Test
    @DisplayName("USER_IP 키는 'ip:{userId}' 형식이어야 한다")
    void user_ip_key_format() {
        assertThat(DetectorRedisKey.USER_IP.getKey(7L)).isEqualTo("ip:7");
    }

    @Test
    @DisplayName("각 키의 prefix 값을 직접 확인한다")
    void prefix_values() {
        assertThat(DetectorRedisKey.BID_COUNT.getPrefix()).isEqualTo("bid:count:");
        assertThat(DetectorRedisKey.VIEW_COUNT.getPrefix()).isEqualTo("view:count:");
        assertThat(DetectorRedisKey.USER_IP.getPrefix()).isEqualTo("ip:");
    }
}
