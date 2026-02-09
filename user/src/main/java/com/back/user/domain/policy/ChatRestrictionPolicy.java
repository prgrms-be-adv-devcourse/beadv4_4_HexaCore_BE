package com.back.user.domain.policy;

import java.time.Duration;

public class ChatRestrictionPolicy {
    public static final int BLIND_THRESHOLD = 3;
    public static final Duration RESTRICT_DURATION = Duration.ofHours(3);
}
