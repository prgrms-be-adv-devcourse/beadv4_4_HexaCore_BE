package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.app.MarketFacade;
import com.back.market.dto.request.BiddingRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class ApiV1MarketController {

    private final MarketFacade marketFacade;

    @PostMapping("/bids/buy")
    public CommonResponse<Long> registerBuyBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 1L;
        Long biddingId = marketFacade.registerBuyBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, biddingId);
    }

    @PostMapping("/bids/sell")
    public CommonResponse<Long> registerSellBid(
            // TODO: 인증 로직 구현 완료시 수정 필요
            // @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid BiddingRequestDto requestDto
    ) {
        // TODO: 인증 적용 시 하드코딩해둔 값 삭제 필요
        // Long userId = userDetails.getId();

        // 임시 하드코딩
        Long userId = 2L;
        Long biddingId = marketFacade.registerSellBid(userId, requestDto);

        return CommonResponse.successWithData(HttpStatus.CREATED, biddingId);
    }

}
