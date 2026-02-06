package com.back.market.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.*;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import com.back.security.principal.AuthPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Market API", description = "상품 구매 및 주문 관련 API")
@RequestMapping("/api/v1/market")
public interface ApiV1Market {
    @Operation(summary = "구매 입찰 등록", description = "구매자가 원하는 가격으로 새로운 구매 입찰을 등록한다.")
    @PostMapping("/bids/buy")
    CommonResponse<MarketPaymentResponseDto> registerBuyBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @Operation(summary = "판매 입찰 등록", description = "판매자가 원하는 가격으로 새로운 판매 입찰을 등록한다.")
    @PostMapping("/bids/sell")
    CommonResponse<MarketPaymentResponseDto> registerSellBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @Operation(summary = "즉시 구매가 조회", description = "특정 상품에 대해 즉시 구매 가능한(판매 입찰 중 가장 낮은) 가격을 조회한다.")
    @GetMapping("/products/{productId}/buy-now-price")
    CommonResponse<InstantBuyPriceResponseDto> getBuyNowPrice(@PathVariable Long productId);

    @Operation(summary = "즉시 판매가 조회", description = "특정 상품에 대해 즉시 판매 가능한(구매 입찰 중 가장 높은) 가격을 조회한다.")
    @GetMapping("/products/{productId}/sell-now-price")
    CommonResponse<InstantSellPriceResponseDto> getSellNowPrice(@PathVariable Long productId);

    @Operation(summary = "즉시 구매 실행", description = "판매 대기 중인 최저가 매물과 매칭하여 즉시 주문을 생성하고 결제를 진행한다.")
    @PostMapping("/buy-now")
    CommonResponse<MarketPaymentResponseDto> buyNow(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @Operation(summary = "즉시 판매 실행", description = "구매 대기 중인 최고가 입찰과 매칭하여 즉시 주문을 생성하고 결제를 진행한다.")
    @PostMapping("/sell-now")
    CommonResponse<MarketPaymentResponseDto> sellNow(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody @Valid BiddingRequestDto requestDto);

    @Operation(summary = "입찰 취소", description = "대기 중인 입찰을 취소하고 구매 입찰일 경우 예치금 환불을 진행한다.")
    @DeleteMapping("/bid/{biddingId}")
    CommonResponse<PaymentCancelResponseDto> cancelBid(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long biddingId
    );

    @Operation(summary = "구매 확정", description = "배송 완료된 주문에 대해 구매 확정을 진행하고 정산 이벤트를 발행한다.")
    @PatchMapping("/orders/{orderId}/complete")
    CommonResponse<Void> completeOrder(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    );

    @Operation(summary = "구매 내역 조회", description = "특정 사용자의 구매 내역을 조회한다.")
    @GetMapping("/orders/buying")
    CommonResponse<Page<OrderListResponseDto>> getBuyingList(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(summary = "판매 내역 조회", description = "특정 사용자의 판매 내역을 조회한다.")
    @GetMapping("/orders/selling")
    CommonResponse<Page<OrderListResponseDto>> getSellingList(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(summary = "구매/판매 상세 내역 조회", description = "특정 사용자의 구매 내역을 조회한다.")
    @GetMapping("/orders/{orderId}")
    CommonResponse<OrderDetailResponseDto> getOrderDetail(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable Long orderId
    );
}
