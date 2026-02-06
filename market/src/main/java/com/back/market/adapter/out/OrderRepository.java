package com.back.market.adapter.out;

import com.back.market.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.sellBidding sb JOIN FETCH sb.marketUser s JOIN FETCH o.buyBidding bb WHERE o.lastModifiedAt BETWEEN :startDate and :endDate")
    List<Order> findSettlementTargetOrders(
            @Param("startDate") LocalDateTime startDateTime,
            @Param("endDate") LocalDateTime endDateTime,
            Pageable pageable
    );

    @Query("SELECT o from Order o JOIN FETCH o.buyBidding bb JOIN FETCH bb.marketProduct WHERE bb.marketUser.id = :userId")
    Page<Order> findSellHistoryList(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o from Order o JOIN FETCH o.sellBidding sb JOIN FETCH o.buyBidding bb JOIN FETCH bb.marketProduct WHERE sb.marketUser.id = :userId")
    Page<Order> findBuyHistoryList(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o from Order o JOIN FETCH o.buyBidding bb JOIN FETCH o.sellBidding sb JOIN FETCH bb.marketProduct WHERE o.id = :orderId")
    Optional<Order> findOrderWithDetails(@Param("orderId") Long orderId);
}
