package com.back.ai.app.usecase;

import com.back.ai.config.AiCacheNames;
import com.back.common.annotation.Loggable;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class EmbeddingUseCase {
    private final EmbeddingModel embeddingModel;

    @Loggable
    @Cacheable(value = AiCacheNames.EMBEDDING, key = "#texts", cacheManager = "aiCacheManager")
    public List<Float> generateEmbeddings(String texts) {
        return convertArrayToList(embeddingModel.embed(texts));
    }

    @Loggable
    public List<Float> convertArrayToList(float[] embedding) {
        return IntStream.range(0, embedding.length)
                        .mapToObj(i -> embedding[i])
                        .toList();
    }
}
