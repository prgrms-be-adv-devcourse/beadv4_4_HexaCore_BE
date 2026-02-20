package com.back.market.event;

import com.back.market.adapter.in.event.MarketKafkaEventListener;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.domain.MarketProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional // 테스트 후 DB 롤백을 위해 필요
class ProductEventConsumeTests {

    @Autowired
    private MarketKafkaEventListener listener;

    @Autowired
    private MarketProductRepository marketProductRepository;

    @Test
    @DisplayName("텍스트 JSON 메시지를 수신하여 DB Insert까지 검증한다")
    void consumeProductEvent_withRawJsonText() {

        String message = """
        {
          "header": {
            "eventId": "test-uuid-001",
            "eventType": "product-item-created",
            "occurrenceAt": "2026-02-20T17:00:00"
          },
          "payload": {
            "productInfo": {
              "productInfoId": 100,
              "brand": {
                "brandId": 10,
                "name": "Nike"
              },
              "category": {
                "categoryId": 20,
                "name": "Shoes"
              },
              "name": "Air Jordan",
              "code": "AJ-001",
              "releasePrice": 150.00,
              "releaseDate": "2026-01-01T00:00:00"
            },
            "options": [
              {
                "group": {
                  "groupId": 1,
                  "groupName": "size"
                },
                "values": [
                  { "valueId": 1001, "valueName": "270" },
                  { "valueId": 1002, "valueName": "280" }
                ]
              }
            ],
            "thumbnailUrl": "https://example.com/image.jpg"
          }
        }
        """;

        listener.consumeProductCreatedEvent(message);

        List<MarketProduct> products = marketProductRepository.findAll();

        // 1. 데이터가 비어있는지 먼저 확인 (실패 시 원인 파악 용이)
        assertThat(products).isNotEmpty();
        assertThat(products).hasSize(2);

        // 2. MarketProduct.java의 'name' 필드 검증 (getName() 사용)
        // payload의 "productName"이 Facade를 거쳐 엔티티의 "name"으로 잘 들어갔는지 확인
        assertThat(products.get(0).getName()).isEqualTo("Air Jordan");

        // 3. productOptionId 검증
        assertThat(products).extracting("productOptionId")
                .containsExactlyInAnyOrder(1001L, 1002L);
    }
}