package com.back.chat.adapter.in.kafka.payload;

import com.back.common.event.KafkaPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BrandCreatedPayload implements KafkaPayload {

    private List<BrandIdOnly> brands;

    @Getter
    @NoArgsConstructor
    public static class BrandIdOnly {
        private Long brandId;
    }
}
