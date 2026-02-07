package com.back.app.usecase;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.image.app.usecase.UploadImageUseCase;
import com.back.image.utils.ImageUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException; // Import for UndeclaredThrowableException
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UploadImageUseCaseTest {

    @Mock
    private ImageUtility imageUtility;

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private UploadImageUseCase uploadImageUseCase;

    @TempDir
    Path tempDir;

    private File dummyFile;
    private String dirName = "test-dir";
    private String bucketName = "test-bucket";
    private String fileExtension = "jpg";
    private String originalFileName = "original.jpg";
    private String expectedS3Url = "https://test-bucket.s3.amazonaws.com/test-app/test-dir/some-uuid.jpg";

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(uploadImageUseCase, "bucket", bucketName);

        dummyFile = Files.createFile(tempDir.resolve(originalFileName)).toFile();
        Files.write(dummyFile.toPath(), "dummy content".getBytes());

        // Default successful mocks for common calls
        when(imageUtility.getFileExtension(anyString())).thenReturn(fileExtension);
        when(amazonS3Client.putObject(any(PutObjectRequest.class))).thenReturn(null);
        when(amazonS3Client.getUrl(anyString(), anyString())).thenReturn(new URL(expectedS3Url));
    }

    // --- Happy Path Tests ---
    @Test
    @DisplayName("단일 이미지를 성공적으로 업로드하고 S3 URL을 반환한다")
    void uploadImage_success() throws IOException {
        // When
        String resultUrl = uploadImageUseCase.uploadImage(dummyFile, dirName);

        // Then
        assertNotNull(resultUrl);
        assertEquals(expectedS3Url, resultUrl);

        verify(imageUtility, times(1)).getFileExtension(originalFileName);
        verify(amazonS3Client, times(1)).putObject(any(PutObjectRequest.class));
        verify(amazonS3Client, times(1)).getUrl(eq(bucketName), anyString());
    }

    @Test
    @DisplayName("여러 이미지를 성공적으로 업로드하고 S3 URL 목록을 반환한다")
    void uploadMultipleImage_success() throws IOException {
        // Given
        File dummyFile2 = Files.createFile(tempDir.resolve("original2.png")).toFile();
        Files.write(dummyFile2.toPath(), "dummy content 2".getBytes());
        List<File> filesToUpload = List.of(dummyFile, dummyFile2);

        when(imageUtility.getFileExtension("original2.png")).thenReturn("png");
        when(amazonS3Client.getUrl(eq(bucketName), argThat(filename -> filename.contains("png"))))
            .thenReturn(new URL("https://test-bucket.s3.amazonaws.com/test-app/test-dir/some-uuid2.png"));

        // When
        List<String> resultUrls = uploadImageUseCase.uploadMultipleImage(filesToUpload, dirName);

        // Then
        assertNotNull(resultUrls);
        assertEquals(2, resultUrls.size());
        assertTrue(resultUrls.contains(expectedS3Url));
        assertTrue(resultUrls.contains("https://test-bucket.s3.amazonaws.com/test-app/test-dir/some-uuid2.png"));

        verify(imageUtility, times(2)).getFileExtension(anyString());
        verify(amazonS3Client, times(2)).putObject(any(PutObjectRequest.class));
        verify(amazonS3Client, times(2)).getUrl(eq(bucketName), anyString());
        Files.deleteIfExists(dummyFile2.toPath());
    }

    // --- Error Handling Tests for uploadImage ---
    @Test
    @DisplayName("uploadImage: 업로드할 파일이 null이거나 존재하지 않으면 CustomException을 발생시킨다")
    void uploadImage_nullOrNonExistentFile_throwsCustomException() {
        // Given
        File nullFile = null;
        File nonExistentFile = tempDir.resolve("non_existent.jpg").toFile();

        // When & Then
        CustomException thrownNull = assertThrows(CustomException.class, () -> uploadImageUseCase.uploadImage(nullFile, dirName));
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrownNull.getFailureCode());
        assertTrue(thrownNull.getMessage().contains("업로드할 파일이 존재하지 않습니다"));


        CustomException thrownNonExistent = assertThrows(CustomException.class, () -> uploadImageUseCase.uploadImage(nonExistentFile, dirName));
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrownNonExistent.getFailureCode());
        assertTrue(thrownNonExistent.getMessage().contains("업로드할 파일이 존재하지 않습니다"));
    }

    @Test
    @DisplayName("uploadImage: buildFileName 중 IOException 발생 시 CustomException을 발생시킨다 (cause 없음)")
    void uploadImage_buildFileNameThrowsIOException_throwsCustomExceptionWithoutCause() throws IOException {
        // Given
        IOException underlyingIOException = new IOException("파일 확장자를 확인할 수 없습니다.");
        when(imageUtility.getFileExtension(anyString())).thenThrow(underlyingIOException);

        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                uploadImageUseCase.uploadImage(dummyFile, dirName)
        );
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        // --- THIS TEST HIGHLIGHTS THE MISSING CAUSE ---
        assertNull(thrown.getCause(), "CustomException should contain the IOException as its cause."); // This will FAIL with current UploadImageUseCase code.
    }

    @Test
    @DisplayName("uploadImage: putS3 중 IOException 발생 시 CustomException을 발생시킨다 (cause 없음)")
    void uploadImage_putS3ThrowsIOException_throwsCustomExceptionWithoutCause() {
        // Given
        AmazonClientException underlyingAwsException = new AmazonClientException("S3 업로드 중 오류 발생"); // Use AWS RuntimeException
        doThrow(underlyingAwsException).when(amazonS3Client).putObject(any(PutObjectRequest.class));

        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                uploadImageUseCase.uploadImage(dummyFile, dirName)
        );
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        // --- THIS TEST HIGHLIGHTS THE MISSING CAUSE ---
        assertNull(thrown.getCause(), "CustomException should contain the IOException from putS3 as its cause."); // This will FAIL with current UploadImageUseCase code.
    }

    @Test
    @DisplayName("uploadImage: S3 URL 가져오는 중 AmazonClientException 발생 시 CustomException을 발생시킨다")
    void uploadImage_getUrlFails_throwsCustomException() {
        // Given
        // Mock getUrl to throw AmazonClientException
        when(amazonS3Client.getUrl(anyString(), anyString())).thenThrow(new AmazonClientException("Failed to get URL"));
        
        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                uploadImageUseCase.uploadImage(dummyFile, dirName) // <--- Changed to uploadImage for direct test
        );
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        verify(amazonS3Client, times(1)).putObject(any(PutObjectRequest.class));
        verify(amazonS3Client, times(1)).getUrl(anyString(), anyString());
    }


    // --- Error Handling Tests for putS3 (via Reflection) ---
    @Test
    @DisplayName("putS3: AmazonClientException 발생 시 IOException으로 다시 throw한다 (cause 포함)")
    void putS3_amazonClientException_throwsIOExceptionWithCause() {
        // Given
        AmazonClientException underlyingAwsException = new AmazonClientException("AWS Client error");
        doThrow(underlyingAwsException).when(amazonS3Client).putObject(any(PutObjectRequest.class));

        // When
        // Invoke the private putS3 method, expecting UndeclaredThrowableException wrapper
        UndeclaredThrowableException thrownWrapper = assertThrows(UndeclaredThrowableException.class, () ->
                ReflectionTestUtils.invokeMethod(uploadImageUseCase, "putS3", dummyFile, "some-file-name.jpg")
        );

        // Then
        Throwable actualCause = thrownWrapper.getCause();
        assertNotNull(actualCause);
        assertTrue(actualCause instanceof IOException); // Assert that the cause is an IOException
        IOException thrownIOException = (IOException) actualCause;
        assertTrue(thrownIOException.getMessage().contains("S3 업로드 중 오류 발생"));
        assertEquals(underlyingAwsException, thrownIOException.getCause()); // Check the cause of the IOException
    }
    
    @Test
    @DisplayName("putS3: AmazonServiceException 발생 시 IOException으로 다시 throw한다 (cause 포함)")
    void putS3_amazonServiceException_throwsIOExceptionWithCause() {
        // Given
        AmazonServiceException underlyingAwsException = new AmazonServiceException("AWS Service error");
        doThrow(underlyingAwsException).when(amazonS3Client).putObject(any(PutObjectRequest.class));

        // When
        UndeclaredThrowableException thrownWrapper = assertThrows(UndeclaredThrowableException.class, () ->
                ReflectionTestUtils.invokeMethod(uploadImageUseCase, "putS3", dummyFile, "some-file-name.jpg")
        );

        // Then
        Throwable actualCause = thrownWrapper.getCause();
        assertNotNull(actualCause);
        assertTrue(actualCause instanceof IOException);
        IOException thrownIOException = (IOException) actualCause;
        assertTrue(thrownIOException.getMessage().contains("S3 업로드 중 오류 발생"));
        assertEquals(underlyingAwsException, thrownIOException.getCause());
    }

    // --- Error Handling Tests for uploadMultipleImage ---
    @Test
    @DisplayName("uploadMultipleImage: 개별 파일 처리 중 CustomException 발생 시 이를 재전파한다 (로깅 확인)")
    void uploadMultipleImage_partialFailure_rethrowsCustomException() throws IOException {
        // Given
        File dummyFile2 = Files.createFile(tempDir.resolve("fail.png")).toFile();
        Files.write(dummyFile2.toPath(), "dummy content 2".getBytes());
        List<File> filesToUpload = List.of(dummyFile, dummyFile2);

        // Mock a failure for the second file in uploadImage
        when(imageUtility.getFileExtension(anyString())) // Default for dummyFile
                .thenReturn(fileExtension)
                .thenThrow(new IOException("Unsupported extension for fail.png")); // For dummyFile2

        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                uploadImageUseCase.uploadMultipleImage(filesToUpload, dirName)
        );
        assertEquals(FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        // Note: The cause of this CustomException will be null because the CustomException in uploadImage doesn't set it.

        Files.deleteIfExists(dummyFile2.toPath());
    }
}