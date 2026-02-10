package com.back.image.app.usecase;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.image.config.AwsS3Properties;
import com.back.image.utils.ImageUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadImageUseCase {

    private final ImageUtility imageUtility;
    private final AmazonS3Client amazonS3Client;
    private final AwsS3Properties awsS3Properties;

    @Loggable
    public List<String> uploadMultipleImage(List<File> resizedFiles, String dirName) {
        return resizedFiles.stream().map(f -> {
            try {
                return uploadImage(f, dirName);
            } catch (CustomException e) {
                log.error("[ImageProcessingFailed] 다중 이미지 업로드 중 개별 파일 처리 실패: {}", e.getMessage(), e);
                throw e;
            }
        }).toList();
    }

    @Loggable
    public String uploadImage(File resizedFile, String dirName) {
        try {
            if (resizedFile == null || !resizedFile.exists()) {
                log.error("[ImageProcessingFailed] 업로드할 파일이 존재하지 않습니다.");
                throw new CustomException("[ImageProcessingFailed] 업로드할 파일이 존재하지 않습니다.", FailureCode.IMAGE_PROCESSING_FAILED);
            }

            String fileName = buildFileName(dirName, resizedFile.getName()); // S3에 저장된 파일 이름

            return putS3(resizedFile, fileName);
        } catch (IOException e) {
            log.error("[ImageProcessingFailed] 이미지 업로드 중 오류 발생", e);
            throw new CustomException("[ImageProcessingFailed] 이미지 업로드 중 오류가 발생했습니다. 원인 : " + e.getMessage(), FailureCode.IMAGE_PROCESSING_FAILED);
        }
    }

    private String putS3(File uploadFile, String fileName) throws IOException {
        try {
            amazonS3Client.putObject(
                    new PutObjectRequest(awsS3Properties.getS3().getBucket(), fileName, uploadFile)
            );

            return amazonS3Client.getUrl(awsS3Properties.getS3().getBucket(), fileName).toString();
        } catch (AmazonClientException e) {
            log.error("[S3UploadFailed] S3 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("[S3UploadFailed] S3 업로드 중 오류 발생", e);
        }
    }

    private String buildFileName(String dirName, String fileName) throws IOException {
        String fileExtension = imageUtility.getFileExtension(fileName);

        String randomFileName = UUID.randomUUID() + "." + fileExtension;

        return dirName + "/" + randomFileName;
    }
}
