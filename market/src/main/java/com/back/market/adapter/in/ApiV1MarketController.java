package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.app.MarketFacade;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ApiV1MarketController implements ApiV1Market{

    private final MarketFacade marketFacade;

    @Override
    public CommonResponse<PayAndHoldResponseDto> registerBuyBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // CustomUserDetails userDetails,
            BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 1L;
        PayAndHoldResponseDto response = marketFacade.registerBuyBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<PayAndHoldResponseDto> registerSellBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // CustomUserDetails userDetails,
            BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 2L;
        PayAndHoldResponseDto response = marketFacade.registerSellBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(Long productId) {
        InstantBuyPriceResponseDto response = marketFacade.getBuyNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    public CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(Long productId) {
        InstantSellPriceResponseDto response = marketFacade.getSellNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    public CommonResponse<PayAndHoldResponseDto> buyNow(BiddingRequestDto requestDto) {
        Long userId = 1L; //TODO: 인증 적용 시 수정
        PayAndHoldResponseDto response = marketFacade.purchaseNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

    @Override
    public CommonResponse<PayAndHoldResponseDto> sellNow(BiddingRequestDto requestDto) {
        Long userId = 2L; //TODO: 인증 적용 시 수정
        PayAndHoldResponseDto response = marketFacade.sellNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, response);
    }

}
