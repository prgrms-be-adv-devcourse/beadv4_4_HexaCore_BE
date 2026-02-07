package com.back.app.usecase;

import com.back.common.exception.CustomException;
import com.back.image.app.usecase.ResizeImageUseCase;
import com.back.image.config.ImageResizeProperties;
import com.back.image.utils.ImageUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResizeImageUseCaseTest {

    @Mock
    private ImageUtility imageUtility;

    @Mock
    private ImageResizeProperties imageResizeProperties;

    @InjectMocks
    private ResizeImageUseCase resizeImageUseCase;

    @TempDir
    Path tempDir; // JUnit 5 provides a temporary directory for tests

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(imageResizeProperties.getWidth()).thenReturn(500);
        when(imageResizeProperties.getHeight()).thenReturn(500);
    }

    private File createDummyImageFile(String filename, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        File outputFile = tempDir.resolve(filename).toFile();
        ImageIO.write(image, "jpg", outputFile);
        return outputFile;
    }

    @Test
    @DisplayName("이미지 크기가 maxWidth보다 작으면 원본 파일을 반환한다")
    void resizeImage_widthLessThanMaxWidth_returnsOriginalFile() throws IOException {
        // Given
        File originalFile = createDummyImageFile("small_image.jpg", 300, 200); // 300 < 500
        when(imageUtility.validateFileExtension(anyString())).thenReturn(true);
        when(imageUtility.getFileExtension(anyString())).thenReturn("jpg");

        // When
        File resultFile = resizeImageUseCase.resizeImage(originalFile);

        // Then
        assertEquals(originalFile, resultFile); // Should return the original file
        verify(imageUtility, never()).validateFileExtension(anyString()); // Validation should not be called
        verify(imageUtility, never()).getFileExtension(anyString());
        // Clean up
        Files.deleteIfExists(originalFile.toPath());
    }

    @Test
    @DisplayName("이미지 크기가 maxWidth보다 크면 리사이즈된 파일을 반환한다")
    void resizeImage_widthGreaterThanMaxWidth_returnsResizedFile() throws IOException {
        // Given
        File originalFile = createDummyImageFile("large_image.jpg", 1000, 800); // 1000 > 500
        when(imageUtility.validateFileExtension(anyString())).thenReturn(true);
        when(imageUtility.getFileExtension(anyString())).thenReturn("jpg");

        // When
        File resultFile = resizeImageUseCase.resizeImage(originalFile);

        // Then
        assertNotEquals(originalFile, resultFile); // Should be a new resized file
        assertTrue(resultFile.exists());
        assertTrue(resultFile.getName().startsWith("resized_"));

        BufferedImage resizedImage = ImageIO.read(resultFile);
        assertEquals(500, resizedImage.getWidth());
        assertEquals(400, resizedImage.getHeight()); // Original ratio 1000/800 = 1.25 -> 500/400 = 1.25

        verify(imageUtility, times(1)).validateFileExtension(resultFile.getName());
        verify(imageUtility, times(1)).getFileExtension(resultFile.getName());

        // Clean up
        Files.deleteIfExists(originalFile.toPath());
        Files.deleteIfExists(resultFile.toPath());
    }

    @Test
    @DisplayName("리사이즈할 파일이 null이거나 존재하지 않으면 IOException을 발생시킨다")
    void resizeImage_nullOrNonExistentFile_throwsIOException() {
        // Given
        File nullFile = null;
        File nonExistentFile = tempDir.resolve("non_existent.jpg").toFile();

        // When & Then
        assertThrows(IOException.class, () -> resizeImageUseCase.resizeImage(nullFile));
        assertThrows(IOException.class, () -> resizeImageUseCase.resizeImage(nonExistentFile));
    }

    @Test
    @DisplayName("파일 확장자를 인식할 수 없을 경우 CustomException을 발생시킨다")
    void resizeImage_unknownFileExtension_throwsCustomException() throws IOException {
        // Given
        File originalFile = createDummyImageFile("image", 1000, 800); // No extension
        when(imageUtility.validateFileExtension(anyString())).thenThrow(new IOException("파일 확장자를 확인할 수 없습니다."));
        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                resizeImageUseCase.resizeMultipleImage(Collections.singletonList(originalFile))
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        // Clean up
        Files.deleteIfExists(originalFile.toPath());
    }

    @Test
    @DisplayName("리사이즈된 파일 생성에 실패하면 IOException을 발생시킨다")
    void resizeImage_failedToCreateResizedFile_throwsIOException() throws IOException {
        // Given
        File originalFile = createDummyImageFile("image.jpg", 1000, 800);
        // Simulate failure by making validateFileExtension throw an IOException
        // or by making ImageIO.write fail (harder to mock for unit tests)
        // For this test, we'll simulate the case where validateFileExtension throws IOException
        // which will be caught and wrapped by resizeMultipleImage.
        when(imageUtility.validateFileExtension(anyString())).thenThrow(new IOException("Simulated validation failure"));
        
        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                resizeImageUseCase.resizeMultipleImage(Collections.singletonList(originalFile))
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        
        // Clean up
        Files.deleteIfExists(originalFile.toPath());
    }

    // Test for multiple files with mixed success/failure (though resizeMultipleImage wraps all IOExceptions)
    @Test
    @DisplayName("여러 이미지 파일 리사이즈 중 하나라도 실패하면 CustomException을 발생시킨다")
    void resizeMultipleImage_partialFailure_throwsCustomException() throws IOException {
        // Given
        File file1 = createDummyImageFile("valid.jpg", 1000, 800);
        File file2 = createDummyImageFile("invalid.png", 600, 600); // Changed dimensions
        List<File> filesToResize = List.of(file1, file2);

        when(imageUtility.validateFileExtension("valid.jpg")).thenReturn(true);
        when(imageUtility.getFileExtension("valid.jpg")).thenReturn("jpg");
        when(imageUtility.validateFileExtension("resized_valid.jpg")).thenReturn(true);
        when(imageUtility.getFileExtension("resized_valid.jpg")).thenReturn("jpg");

        // Mocks for file2 (invalid case)
        // For "invalid.png", assume getFileExtension can still extract "png",
        // but validateFileExtension should return FALSE for unsupported format.
        // The file name generated in resizeImage for file2 will be "resized_invalid.png"
        when(imageUtility.validateFileExtension(startsWith("resized_invalid.png"))).thenThrow(new IOException("Simulated unsupported format"));
        when(imageUtility.getFileExtension(startsWith("resized_invalid.png"))).thenReturn("png"); // <--- MOCK THIS TO AVOID NULL for ImageIO.write
        
        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                resizeImageUseCase.resizeMultipleImage(filesToResize)
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
        verify(imageUtility, times(1)).validateFileExtension(startsWith("resized_invalid.png"));
        
        // Clean up all potential temporary files
        Files.deleteIfExists(file1.toPath());
        Files.deleteIfExists(file2.toPath());
        Files.walk(tempDir)
            .filter(Files::isRegularFile)
            .filter(p -> p.getFileName().toString().startsWith("resized_"))
            .forEach(p -> {
                try { Files.delete(p); } catch (IOException e) { /* ignore */ }
            });
    }
}
