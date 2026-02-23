package com.back.market.event;

import com.back.market.adapter.in.event.MarketKafkaEventListener;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.domain.MarketProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @DisplayName("추가 이벤트 수신 시 텍스트 JSON 메시지를 수신하여 DB Insert까지 검증한다")
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
        assertThat(products).extracting("id")
                .containsExactlyInAnyOrder(1001L, 1002L);
    }

    @Test
    @DisplayName("수정 이벤트 수신 시 기존 상품 정보가 업데이트되는지 검증한다")
    void consumeUpdatedEvent_Success() {
        // 1. 기존 데이터 준비 (ID: 1001)
        MarketProduct initialProduct = MarketProduct.builder()
                .id(1001L)
                .productInfoId(100L)
                .name("Old Name")
                .brandName("Nike")
                .categoryName("Shoes")
                .productNumber("AJ-001")
                .productOption("270")
                .releasePrice(new BigDecimal("100.00"))
                .build();
        marketProductRepository.save(initialProduct);

        // 2. Envelope 패턴의 수정 JSON (이름과 가격 변경)
        String updateMessage = """
        {
          "header": { "eventId": "upd-001", "eventType": "product-item-updated" },
          "payload": {
            "productInfo": {
              "productInfoId": 100,
              "brand": { "brandId": 10, "name": "Nike" },
              "category": { "categoryId": 20, "name": "Shoes" },
              "name": "New Air Jordan",
              "code": "AJ-001-NEW",
              "releasePrice": 200.00
            },
            "options": [
              { "values": [{ "valueId": 1001, "valueName": "270" }] }
            ],
            "thumbnailUrl": "https://example.com/new-image.jpg"
          }
        }
        """;

        // 3. 수신
        listener.consumeProductUpdatedEvent(updateMessage);

        // 4. 검증
        MarketProduct updatedProduct = marketProductRepository.findById(1001L).orElseThrow();
        assertThat(updatedProduct.getName()).isEqualTo("New Air Jordan");
        assertThat(updatedProduct.getProductNumber()).isEqualTo("AJ-001-NEW");
        assertThat(updatedProduct.getReleasePrice()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("삭제 이벤트 수신 시 Soft Delete 되어 조회되지 않는지 검증한다")
    void consumeDeletedEvent_SoftDeleteSuccess() {
        // 1. 기존 데이터 준비 (InfoId: 100 산하 2개 옵션)
        marketProductRepository.save(MarketProduct.builder().id(1001L).productInfoId(100L).name("P1").brandName("B").categoryName("C").productNumber("N1").productOption("270").build());
        marketProductRepository.save(MarketProduct.builder().id(1002L).productInfoId(100L).name("P1").brandName("B").categoryName("C").productNumber("N1").productOption("280").build());

        // 2. 삭제 JSON
        String deleteMessage = """
        {
          "header": { "eventId": "del-001", "eventType": "product-item-deleted" },
          "payload": {
            "productInfoId": 100
          }
        }
        """;

        // 3. 수신
        listener.consumeProductDeletedEvent(deleteMessage);

        // 4. 검증
        // findAll()은 @SQLRestriction("deleted_at IS NULL") 때문에 삭제된 건을 가져오지 않아야 함
        List<MarketProduct> products = marketProductRepository.findAll();
        assertThat(products).isEmpty();

        // 하지만 DB에는 존재해야 함 (Native Query 등으로 확인 가능하나, 여기선 갯수 0으로 로직 성공 확인)
    }
}