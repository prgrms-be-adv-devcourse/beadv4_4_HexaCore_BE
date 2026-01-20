package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.app.MarketFacade;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class ApiV1MarketController implements ApiV1Market{

    private final MarketFacade marketFacade;

    @Override
    @PostMapping("/bids/buy")
    public CommonResponse<Long> registerBuyBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // CustomUserDetails userDetails,
            BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 1L;
        Long biddingId = marketFacade.registerBuyBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, biddingId);
    }

    @Override
    @PostMapping("/bids/sell")
    public CommonResponse<Long> registerSellBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // CustomUserDetails userDetails,
            BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 2L;
        Long biddingId = marketFacade.registerSellBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, biddingId);
    }

    @Override
    @GetMapping("/products/{productId}/buy-now-price")
    public CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(Long productId) {
        InstantBuyPriceResponseDto response = marketFacade.getBuyNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    @GetMapping("/products/{productId}/sell-now-price")
    public CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(Long productId) {
        InstantSellPriceResponseDto response = marketFacade.getSellNowPrice(productId);
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }

    @Override
    @PostMapping("/buy-now")
    public CommonResponse<Long> buyNow(BiddingRequestDto requestDto) {
        Long userId = 1L; //TODO: 인증 적용 시 수정
        Long orderId = marketFacade.purchaseNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, orderId);
    }

    @Override
    @PostMapping("/sell-now")
    public CommonResponse<Long> sellNow(BiddingRequestDto requestDto) {
        Long userId = 2L; //TODO: 인증 적용 시 수정
        Long orderId = marketFacade.sellNow(userId, requestDto);
        return CommonResponse.successWithData(HttpStatus.CREATED, orderId);
    }
    
}
