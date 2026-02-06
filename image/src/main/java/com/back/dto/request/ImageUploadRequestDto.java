package com.back.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record ImageUploadRequestDto(
        @NotEmpty(message = "Uploaded files cannot be empty")
        List<MultipartFile> files
) {
}
