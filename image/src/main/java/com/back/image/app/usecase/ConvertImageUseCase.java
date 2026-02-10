package com.back.image.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.image.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvertImageUseCase {
    private final ImageUtility imageUtility;

    @Loggable(logArgs = false)
    public List<File> convertMultipleFile(List<MultipartFile> multipartFiles) {
        return multipartFiles.stream().map(mf -> {
            try {
                return convertFile(mf);
            } catch (CustomException e) {
                log.error("[ImageProcessingFailed] 다중 이미지 파일 변환 실패 : {}", e.getMessage(), e);
                throw e;
            }
        }).toList();
    }

    @Loggable(logArgs = false)
    public File convertFile(MultipartFile multipartFile) {
        try {
            if (multipartFile == null || multipartFile.isEmpty()) {
                log.error("[ImageProcessingFailed] 변환할 파일이 존재하지 않습니다.");
                throw new CustomException("[ImageProcessingFailed] 변환할 파일이 존재하지 않습니다.", FailureCode.IMAGE_PROCESSING_FAILED);
            }

            if (!imageUtility.validateFileExtension(multipartFile.getOriginalFilename())) {
                log.error("[ImageProcessingFailed] 지원하지 않는 파일 형식입니다.");
                throw new CustomException("[ImageProcessingFailed] 지원하지 않는 파일 형식입니다.", FailureCode.IMAGE_PROCESSING_FAILED);
            }

            String uniqueFileName = buildUniqueFileName(multipartFile.getOriginalFilename());
            File originalTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + uniqueFileName);

            try (FileOutputStream fos = new FileOutputStream(originalTempFile)) {
                fos.write(multipartFile.getBytes());
            } catch (IOException e) {
                log.error("[ImageProcessingFailed] 파일 업로드를 위한 임시 파일 생성에 실패했습니다. : {}", e.getMessage(), e);
                throw new CustomException("[ImageProcessingFailed] 파일 업로드를 위한 임시 파일 생성에 실패했습니다. : " + e.getMessage(), FailureCode.IMAGE_PROCESSING_FAILED);
            }

            return originalTempFile;
        } catch (IOException e) {
            log.error("[ImageProcessingFailed] 이미지 파일 변환 중 IOException 발생하였습니다. : {}", e.getMessage(), e);
            throw new CustomException("[ImageProcessingFailed] 이미지 파일 중 IOException 발생하였습니다. : " + e.getMessage(), FailureCode.IMAGE_PROCESSING_FAILED);
        }
    }

    private String buildUniqueFileName(String originalFilename) throws IOException {
        String fileExtension = imageUtility.getFileExtension(originalFilename);
        String uniqueID = UUID.randomUUID().toString();
        return uniqueID + "." + fileExtension;
    }
}
