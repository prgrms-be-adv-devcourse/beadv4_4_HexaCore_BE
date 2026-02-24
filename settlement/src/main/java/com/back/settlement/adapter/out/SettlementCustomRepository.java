package com.back.settlement.adapter.out;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementCustomRepository {
    Page<Settlement> findSettlementsByFilters(
            SettlementStatus status,
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<SettlementItem> findItemsByFilters(
            Long payeeId,
            Long orderId,
            Long productId,
            SettlementItemStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    List<Long> findDistinctUnsettledSellerIds(LocalDateTime startAt, LocalDateTime endAt, Long systemPayeeId);

    List<SettlementItem> findUnsettledItemsByPayeeIds(List<Long> payeeIds, LocalDateTime startAt, LocalDateTime endAt);
}
