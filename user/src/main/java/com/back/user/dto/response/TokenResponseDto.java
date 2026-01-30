package com.back.user.dto.response;

import java.time.Duration;

public record TokenResponseDto(String accessToken, String refreshToken, Duration refreshTtl) {}
