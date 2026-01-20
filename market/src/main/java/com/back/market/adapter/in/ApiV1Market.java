package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Market API", description = "상품 구매 및 주문 관련 API")
public interface ApiV1Market {
    @PostMapping("/bids/buy")
    CommonResponse<Long> registerBuyBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @PostMapping("/bids/sell")
    CommonResponse<Long> registerSellBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @GetMapping("/products/{productId}/buy-now-price")
    CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(@PathVariable Long productId);

    @GetMapping("/products/{productId}/sell-now-price")
    CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(@PathVariable Long productId);

    @PostMapping("/buy-now")
    CommonResponse<Long> buyNow(
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @PostMapping("/sell-now")
    CommonResponse<Long> sellNow(
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

}
