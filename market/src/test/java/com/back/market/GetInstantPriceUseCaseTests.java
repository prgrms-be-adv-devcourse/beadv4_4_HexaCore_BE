package com.back.market;

import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.mapper.BiddingMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetInstantPriceUseCaseTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private BiddingRepository biddingRepository;
    @Autowired private MarketUserRepository marketUserRepository;
    @Autowired private MarketProductRepository marketProductRepository;
    @Autowired private BiddingMapper biddingMapper;

    @Test
    @DisplayName("상품의 즉시 구매가(최저 판매 입찰가)를 조회한다")
    void getBuyNowPrice_Success() throws Exception {
        Long productId = 100L;
        Long sellerId = 1L;

        createBidding(productId, sellerId, 200000, BiddingPosition.SELL);
        createBidding(productId, sellerId, 180000, BiddingPosition.SELL); // 정답 (최저가)
        createBidding(productId, sellerId, 220000, BiddingPosition.SELL);

        mockMvc.perform(get("/api/v1/market/product/{productId}/buy-now-price", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.buyNowPrice").value(180000));
    }

    @Test
    @DisplayName("상품의 즉시 판매가(최고 구매 입찰가)를 조회한다")
    void getSellNowPrice_Success() throws Exception {
        Long productId = 100L;
        Long buyerId = 2L;

        createBidding(productId, buyerId, 150000, BiddingPosition.BUY);
        createBidding(productId, buyerId, 160000, BiddingPosition.BUY); // 정답 (최고가)
        createBidding(productId, buyerId, 140000, BiddingPosition.BUY);

        mockMvc.perform(get("/api/v1/market/product/{productId}/sell-now-price", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.sellNowPrice").value(160000));
    }

    @Test
    @DisplayName("등록된 입찰이 없으면 가격은 null을 반환한다")
    void getPrice_WhenNoBids() throws Exception {
        Long productId = 200L;

        mockMvc.perform(get("/api/v1/market/product/{productId}/buy-now-price", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.buyNowPrice").isEmpty());

        mockMvc.perform(get("/api/v1/market/product/{productId}/sell-now-price", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sellNowPrice").isEmpty());
    }

    // --- Helper Methods ---

    private void createBidding(Long productId, Long userId, long price, BiddingPosition position) {
        MarketProduct product = marketProductRepository.findById(productId).orElseThrow();
        MarketUser user = marketUserRepository.findById(userId).orElseThrow();

        BiddingRequestDto requestDto = new BiddingRequestDto(productId, BigDecimal.valueOf(price), "270");

        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, position);

        biddingRepository.save(bidding);
    }
}
