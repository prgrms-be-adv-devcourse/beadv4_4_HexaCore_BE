package com.back.market.adapter.out;

import com.back.market.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o JOIN FETCH o.sellBidding sb JOIN FETCH sb.marketUser s JOIN FETCH o.buyBidding bb WHERE o.lastModifiedAt BETWEEN :startDate and :endDate")
    List<Order> findSettlementTargetOrders(
            @Param("startDate") LocalDateTime startDateTime,
            @Param("endDate") LocalDateTime endDateTime,
            Pageable pageable
    );
}
