package com.back.market.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.market.app.MarketFacade;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import com.back.market.dto.response.MarketPaymentResponseDto;
import com.back.security.principal.AuthPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApiV1MarketController implements ApiV1Market{

    private final MarketFacade marketFacade;

    @Override
    public CommonResponse<MarketPaymentResponseDto> registerBuyBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto
    ) {
        Long userId = principal.getUserId();

        MarketPaymentResponseDto response = marketFacade.registerBuyBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<MarketPaymentResponseDto> registerSellBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto
    ) {
        Long userId = principal.getUserId();

        MarketPaymentResponseDto response = marketFacade.registerSellBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(@PathVariable Long productId) {
        InstantBuyPriceResponseDto response = marketFacade.getBuyNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    public CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(@PathVariable Long productId) {
        InstantSellPriceResponseDto response = marketFacade.getSellNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    public CommonResponse<MarketPaymentResponseDto> buyNow(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto) {

        Long userId = principal.getUserId();

        MarketPaymentResponseDto response = marketFacade.purchaseNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<MarketPaymentResponseDto> sellNow(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto) {

        Long userId = principal.getUserId();

        MarketPaymentResponseDto response = marketFacade.sellNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<PaymentCancelResponseDto> cancelBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long biddingId) {

        Long userId = principal.getUserId();

        PaymentCancelResponseDto result = marketFacade.cancelBid(userId, biddingId);
        return CommonResponse.success(SuccessCode.OK, result);
    }


}
