package com.back.ai.app.usecase;

import com.back.common.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingUseCase {
    private final EmbeddingModel embeddingModel;

    @Loggable
    public float[] generateEmbeddings(String texts) {
        return embeddingModel.embed(texts);
    }
}
