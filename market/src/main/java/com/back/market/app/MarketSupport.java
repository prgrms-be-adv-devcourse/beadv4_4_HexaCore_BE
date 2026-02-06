package com.back.market.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.*;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * repository에 있는 조회 메서드들을 관리하는 클래스
 */
@Component
@RequiredArgsConstructor
public class MarketSupport {
    private final MarketUserRepository marketUserRepository;
    private final BiddingRepository biddingRepository;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MarketProductRepository marketProductRepository;

    /**
     * Bidding 엔티티 조회
     * @param biddingId PK
     * @return Bidding
     */
    public Bidding findBiddingById(Long biddingId) {
        return biddingRepository.findById(biddingId)
                .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));
    }

    /**
     * Order 엔티티 조회
     * @param orderId PK
     * @return Order
     */
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException(FailureCode.ORDER_NOT_FOUND));
    }

    /**
     * MarketUser의 Cart 존재 여부 확인
     * @param user MarketUser
     * @return boolean
     */
    public boolean existsCartByMarketUser(MarketUser user) {
        return cartRepository.existsByMarketUser(user);
    }

    /**
     * MarketUser 엔티티 조회
     * @param userId PK
     * @return MarketUser
     */
    public MarketUser findMarketUserById(Long userId) {
        return marketUserRepository.findById(userId).orElseThrow(() -> new BadRequestException(FailureCode.USER_NOT_FOUND));
    }

    /**
     * MarketUser 엔티티 조회(Optional)
     * @param userId PK
     * @return MarketUser
     */
    public Optional<MarketUser> findMarketUserByIdOptional(Long userId) {
        return marketUserRepository.findById(userId);
    }

    /**
     * MarketProduct 존재 여부 확인
     * @param productId PK
     * @return boolean
     */
    public boolean existsByMarketProduct(Long productId) {
        return marketProductRepository.existsById(productId);
    }

    /**
     * MarketProduct 엔티티 조회
     * @param productId PK
     * @return MarketProduct
     */
    public MarketProduct findMarketProductById(Long productId) {
        return marketProductRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(FailureCode.PRODUCT_NOT_FOUND));
    }

    /**
     * Settlement 대상 주문 조회
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @param pageable 페이지
     * @return List<Order>
     */
    public List<Order> findSettlementTargetOrders(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        return orderRepository.findSettlementTargetOrders(startDateTime, endDateTime, pageable);
    }

    /**
     * 즉시 구매가 조회(최저가 판매 입찰 조회)
     * @param productId 상품 PK
     * @param position 구매/판매
     * @param status 구매 상태
     * @return Optional<Bidding>
     */
    public Optional<Bidding> findInstantBuyPrice(Long productId, BiddingPosition position, BiddingStatus status) {
        return biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(productId, position, status);
    }

    /**
     * 즉시 판매가 조회(최고가 구매 입찰 조회)
     * @param productId 상품 PK
     * @param position 구매/판매
     * @param status 구매 상태
     * @return Optional<Bidding>
     */
    public Optional<Bidding> findInstantSellPrice(Long productId, BiddingPosition position, BiddingStatus status) {
        return biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(productId, position, status);
    }

    /**
     * 구매 내역 목록 조회
     * @param userId 사용자 id
     * @param pageable 페이징
     * @return Page<Order>
     */
    public Page<Order> findBuyHistoryList(Long userId, Pageable pageable) {
        return orderRepository.findBuyHistoryList(userId, pageable);
    }

    /**
     * 판매 내역 목록 조회
     * @param userId 사용자 id
     * @param pageable 페이징
     * @return Page<Order>
     */
    public Page<Order> findSellHistoryList(Long userId, Pageable pageable) {
        return orderRepository.findSellHistoryList(userId, pageable);
    }

    /**
     * 주문 상세 조회
     * @param orderId 주문 PK
     * @return Order
     */
    public Order findOrderWithDetails(Long orderId) {
        return orderRepository.findByIdWithDetails(orderId).orElseThrow(() -> new BadRequestException(FailureCode.ORDER_NOT_FOUND));
    }

}
