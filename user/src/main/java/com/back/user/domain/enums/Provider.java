package com.back.user.domain.enums;

import java.util.Arrays;

public enum Provider {
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String key;

    Provider(String key) { this.key = key; }

    public static Provider fromKey(String key) {
        return Arrays.stream(values())
                .filter(p -> p.key.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider: " + key));
    }
}
