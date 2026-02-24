package com.back.app.usecase;

import com.back.common.exception.CustomException;
import com.back.image.app.usecase.ConvertImageUseCase;
import com.back.image.utils.ImageUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class ConvertImageUseCaseTest {

    @Mock
    private ImageUtility imageUtility;

    @InjectMocks
    private ConvertImageUseCase convertImageUseCase;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Ensure that System.getProperty("java.io.tmpdir") returns our temporary directory
        // This is a bit tricky as System.setProperty affects the whole JVM.
        // For actual use, consider a more isolated test environment or
        // passing the temp directory directly to the UseCase if possible.
        // For now, we'll assume the files are created in the system temp directory and
        // we'll clean them up.
    }

    @Test
    @DisplayName("여러 MultipartFile을 File로 성공적으로 변환한다")
    void convertMultipleFile_success() throws IOException {
        // Given
        String filename1 = "test1.jpg";
        String filename2 = "test2.png";
        MockMultipartFile multipartFile1 = new MockMultipartFile("file", filename1, "image/jpeg", "test image content".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("file", filename2, "image/png", "another test image content".getBytes());
        List<MultipartFile> multipartFiles = List.of(multipartFile1, multipartFile2);

        when(imageUtility.validateFileExtension(anyString())).thenReturn(true);
        when(imageUtility.getFileExtension(contains("test1.jpg"))).thenReturn("jpg");
        when(imageUtility.getFileExtension(contains("test2.png"))).thenReturn("png");

        // When
        List<File> convertedFiles = convertImageUseCase.convertMultipleFile(multipartFiles);

        // Then
        assertNotNull(convertedFiles);
        assertEquals(2, convertedFiles.size());
        assertTrue(convertedFiles.stream().anyMatch(f -> f.getName().endsWith(".jpg")));
        assertTrue(convertedFiles.stream().anyMatch(f -> f.getName().endsWith(".png")));
        convertedFiles.forEach(file -> {
            assertTrue(file.exists());
            assertTrue(file.length() > 0);
            // Clean up created temporary files
            file.delete();
        });

        verify(imageUtility, times(2)).validateFileExtension(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 파일 확장자일 경우 CustomException을 발생시킨다")
    void convertMultipleFile_invalidExtension_throwsCustomException() throws IOException {
        // Given
        String filename = "invalid.txt";
        MockMultipartFile multipartFile = new MockMultipartFile("file", filename, "text/plain", "text content".getBytes());
        List<MultipartFile> multipartFiles = Collections.singletonList(multipartFile);

        when(imageUtility.validateFileExtension(anyString())).thenReturn(false);

        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                convertImageUseCase.convertMultipleFile(multipartFiles)
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());

        verify(imageUtility, times(1)).validateFileExtension(anyString());
    }

    @Test
    @DisplayName("파일이 비어있을 경우 CustomException을 발생시킨다")
    void convertFile_emptyFile_throwsCustomException() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        // When & Then
        assertThrows(CustomException.class, () ->
                convertImageUseCase.convertFile(emptyFile)
        );
    }

    @Test
    @DisplayName("MultipartFile이 null일 경우 CustomException을 발생시킨다")
    void convertFile_nullMultipartFile_throwsCustomException() {
        // Given
        MultipartFile nullFile = null;

        // When & Then
        assertThrows(CustomException.class, () ->
                convertImageUseCase.convertFile(nullFile)
        );
    }

    @Test
    @DisplayName("파일 쓰기 중 IOException 발생 시 CustomException을 발생시킨다")
    void convertMultipleFile_writeError_throwsCustomException() throws IOException {
        // Given
        String filename = "error.jpg";
        MockMultipartFile multipartFile = new MockMultipartFile("file", filename, "image/jpeg", "test content".getBytes());
        List<MultipartFile> multipartFiles = Collections.singletonList(multipartFile);

        when(imageUtility.validateFileExtension(anyString())).thenReturn(true);

        // To simulate IOException during file writing, we can't easily mock FileOutputStream within the method under test.
        // This test case relies on the underlying system's ability to throw an IOException,
        // or a more complex mocking setup for File.
        // For this unit test, we focus on the validation and the outer CustomException.
        // A more integration-style test might be needed to fully cover FileOutputStream exceptions.

        // For now, we'll just test the CustomException wrapping for the IOException
        // as the convertMultipleFile catches IOException and rethrows CustomException.
        // We cannot directly mock FileOutputStream's constructor or write method here simply.
        // Assuming convertFile throws an IOException for *some* reason (e.g. permission denied on temp dir)
        // the convertMultipleFile should wrap it.

        // As a workaround for testing the IOException wrapping:
        // Mock the ImageUtility to throw an IOException during validation to simulate an early IOException.
        // Or, more accurately, we would need to ensure `convertFile` throws IOException,
        // which would then be caught by `convertMultipleFile`
        when(imageUtility.validateFileExtension(anyString())).thenThrow(new IOException("Simulated write error")); // Simulating underlying IOException for wrapping

        CustomException thrown = assertThrows(CustomException.class, () ->
                convertImageUseCase.convertMultipleFile(multipartFiles)
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
    }
}
