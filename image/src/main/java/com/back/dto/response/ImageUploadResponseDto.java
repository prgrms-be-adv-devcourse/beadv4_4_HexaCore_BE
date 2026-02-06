package com.back.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ImageUploadResponseDto(
        List<String> fileUrl
) {
}
