package com.back.app.usecase;

import com.back.common.exception.CustomException;
import com.back.image.app.usecase.ResizeImageUseCase;
import com.back.image.config.ImageResizeProperties;
import com.back.image.utils.ImageUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ResizeImageUseCaseTest {

    @Mock
    private ImageUtility imageUtility;

    @Mock
    private ImageResizeProperties imageResizeProperties;

    @InjectMocks
    private ResizeImageUseCase resizeImageUseCase;

    @TempDir
    Path tempDir;

    private List<File> filesToDelete = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(imageResizeProperties.getWidth()).thenReturn(500);
        when(imageResizeProperties.getHeight()).thenReturn(500);
    }

    @AfterEach
    void tearDown() {
        for (File file : filesToDelete) {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    private File createDummyImageFile(String filename, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        File outputFile = tempDir.resolve(filename).toFile();
        ImageIO.write(image, "jpg", outputFile);
        return outputFile;
    }

    @Test
    @DisplayName("이미지 리사이징 시 항상 'resized_' 접두사가 붙은 새로운 파일을 생성한다")
    void resizeImage_alwaysReturnsNewResizedFile() throws IOException {
        // Given
        File originalFile = createDummyImageFile("test_image.jpg", 1000, 800);

        // When
        File resultFile = resizeImageUseCase.resizeImage(originalFile);
        filesToDelete.add(resultFile);

        // Then
        assertNotEquals(originalFile.getAbsolutePath(), resultFile.getAbsolutePath());
        assertTrue(resultFile.getName().startsWith("resized_"));
        assertTrue(resultFile.exists());

        BufferedImage resizedImage = ImageIO.read(resultFile);
        // Thumbnails.size(500, 500)은 비율을 유지하면서 500x500 안에 들어가도록 조정함
        assertEquals(500, resizedImage.getWidth());
        assertEquals(400, resizedImage.getHeight()); // 1000:800 = 500:400
        resizedImage.flush();
    }

    @Test
    @DisplayName("여러 이미지 파일 리사이즈를 성공적으로 수행한다")
    void resizeMultipleImage_success() throws IOException {
        // Given
        File file1 = createDummyImageFile("image1.jpg", 1000, 1000);
        File file2 = createDummyImageFile("image2.jpg", 800, 600);
        List<File> filesToResize = List.of(file1, file2);

        // When
        List<File> results = resizeImageUseCase.resizeMultipleImage(filesToResize);
        filesToDelete.addAll(results);

        // Then
        assertEquals(2, results.size());
        for (File result : results) {
            assertTrue(result.getName().startsWith("resized_"));
            assertTrue(result.exists());
        }
    }

    @Test
    @DisplayName("리사이즈할 파일이 null이거나 존재하지 않으면 CustomException을 발생시킨다")
    void resizeImage_nullOrNonExistentFile_throwsCustomException() {
        // Given
        File nullFile = null;
        File nonExistentFile = new File(tempDir.toFile(), "non_existent.jpg");

        // When & Then
        assertThrows(CustomException.class, () -> resizeImageUseCase.resizeImage(nullFile));
        assertThrows(CustomException.class, () -> resizeImageUseCase.resizeImage(nonExistentFile));
    }

    @Test
    @DisplayName("이미지 리사이징 중 IOException이 발생하면 CustomException으로 래핑하여 던진다")
    void resizeImage_ioException_throwsCustomException() throws IOException {
        // Given
        // 읽을 수 없는 파일을 생성하여 IOException 유도
        File corruptedFile = tempDir.resolve("corrupted.jpg").toFile();
        Files.write(corruptedFile.toPath(), new byte[]{0, 1, 2, 3}); 

        // When & Then
        CustomException thrown = assertThrows(CustomException.class, () ->
                resizeImageUseCase.resizeImage(corruptedFile)
        );
        assertEquals(com.back.common.code.FailureCode.IMAGE_PROCESSING_FAILED, thrown.getFailureCode());
    }
}
