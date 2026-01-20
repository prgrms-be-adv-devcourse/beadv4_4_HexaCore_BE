package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ApiV1Market {
    CommonResponse<Long> registerBuyBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    CommonResponse<Long> registerSellBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(@PathVariable Long productId);

    CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(@PathVariable Long productId);

    CommonResponse<Long> buyNow(
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

    CommonResponse<Long> sellNow(
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto);

}
