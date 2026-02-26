package com.back.ai.app.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmbeddingUseCase 단위 테스트")
class EmbeddingUseCaseTest {

    @InjectMocks
    private EmbeddingUseCase embeddingUseCase;

    @Mock
    private EmbeddingModel embeddingModel;

    @Nested
    @DisplayName("generateEmbeddings 메서드")
    class GenerateEmbeddingsTest {

        @Test
        @DisplayName("성공: 주어진 텍스트에 대한 임베딩을 성공적으로 생성한다")
        void generateEmbeddings_Success() {
            // given
            String inputText = "Hello, world!";
            List<Float> expectedEmbedding = List.of(0.1f, 0.2f, 0.3f);
            given(embeddingModel.embed(inputText)).willReturn(new float[]{0.1f, 0.2f, 0.3f});

            // when
            List<Float> result = embeddingUseCase.generateEmbeddings(inputText);

            // then
            assertThat(result).isEqualTo(expectedEmbedding);
            verify(embeddingModel).embed(inputText);
        }

        @Test
        @DisplayName("실패: 외부 API 호출 실패 시 예외가 발생한다")
        void generateEmbeddings_ApiFailure() {
            // given
            String inputText = "This will fail.";
            given(embeddingModel.embed(anyString())).willThrow(new RuntimeException("API Error"));

            // when & then
            assertThatThrownBy(() -> embeddingUseCase.generateEmbeddings(inputText))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("API Error");
            verify(embeddingModel).embed(inputText);
        }

        @Test
        @DisplayName("엣지 케이스: 빈 문자열이 주어졌을 때 정상적으로 처리한다")
        void generateEmbeddings_EmptyString() {
            // given
            String inputText = "";
            List<Float> expectedEmbedding = List.of(0.0f, 0.0f, 0.0f); // Assuming the model returns a zero vector for empty string
            given(embeddingModel.embed(inputText)).willReturn(new float[]{0.0f, 0.0f, 0.0f});

            // when
            List<Float> result = embeddingUseCase.generateEmbeddings(inputText);

            // then
            assertThat(result).isEqualTo(expectedEmbedding);
            verify(embeddingModel).embed(inputText);
        }
        
        @Test
        @DisplayName("엣지 케이스: 매우 긴 텍스트가 주어졌을 때 정상적으로 처리한다")
        void generateEmbeddings_LongString() {
            // given
            String longText = "a".repeat(10000);
            List<Float> expectedEmbedding = List.of(0.4f, 0.5f, 0.6f);
            given(embeddingModel.embed(longText)).willReturn(new float[]{0.4f, 0.5f, 0.6f});

            // when
            List<Float> result = embeddingUseCase.generateEmbeddings(longText);

            // then
            assertThat(result).isEqualTo(expectedEmbedding);
            verify(embeddingModel).embed(longText);
        }
    }

    @Nested
    @DisplayName("convertArrayToList 메서드")
    class ConvertArrayToListTest {

        @Test
        @DisplayName("성공: float 배열을 List<Float>로 성공적으로 변환한다")
        void convertArrayToList_Success() {
            // given
            float[] inputArray = {0.1f, -0.2f, 3.14f};
            List<Float> expectedList = List.of(0.1f, -0.2f, 3.14f);

            // when
            List<Float> result = embeddingUseCase.convertArrayToList(inputArray);

            // then
            assertThat(result).isEqualTo(expectedList);
        }

        @Test
        @DisplayName("엣지 케이스: 빈 배열이 주어졌을 때 빈 리스트를 반환한다")
        void convertArrayToList_EmptyArray() {
            // given
            float[] inputArray = {};

            // when
            List<Float> result = embeddingUseCase.convertArrayToList(inputArray);

            // then
            assertThat(result).isNotNull().isEmpty();
        }
    }
}
