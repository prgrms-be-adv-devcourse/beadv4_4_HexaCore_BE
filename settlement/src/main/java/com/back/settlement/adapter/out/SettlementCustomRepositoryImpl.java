package com.back.settlement.adapter.out;

import static com.back.settlement.domain.QSettlement.settlement;
import static com.back.settlement.domain.QSettlementItem.settlementItem;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementItemStatus;
import com.back.settlement.domain.SettlementStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SettlementCustomRepositoryImpl implements SettlementCustomRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Settlement> findSettlementsByFilters(
            SettlementStatus status,
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        List<Settlement> content = queryFactory
                .selectFrom(settlement)
                .where(
                        statusEq(status),
                        sellerIdEq(sellerId),
                        settlementCreatedAtGoe(startDate),
                        settlementCreatedAtLoe(endDate)
                )
                .orderBy(settlement.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(settlement.count())
                .from(settlement)
                .where(
                        statusEq(status),
                        sellerIdEq(sellerId),
                        settlementCreatedAtGoe(startDate),
                        settlementCreatedAtLoe(endDate)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<SettlementItem> findItemsByFilters(
            Long payeeId,
            Long orderId,
            Long productId,
            SettlementItemStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    ) {
        List<SettlementItem> content = queryFactory
                .selectFrom(settlementItem)
                .where(
                        payeeIdEq(payeeId),
                        orderIdEq(orderId),
                        productIdEq(productId),
                        itemStatusEq(status),
                        confirmedAtGoe(startDate),
                        confirmedAtLoe(endDate)
                )
                .orderBy(settlementItem.confirmedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(settlementItem.count())
                .from(settlementItem)
                .where(
                        payeeIdEq(payeeId),
                        orderIdEq(orderId),
                        productIdEq(productId),
                        itemStatusEq(status),
                        confirmedAtGoe(startDate),
                        confirmedAtLoe(endDate)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public List<Long> findDistinctUnsettledSellerIds(LocalDateTime startAt, LocalDateTime endAt, Long systemPayeeId) {
        return queryFactory
                .select(settlementItem.payeeId)
                .distinct()
                .from(settlementItem)
                .where(
                        settlementItem.status.eq(SettlementItemStatus.COLLECTED),
                        settlementItem.payeeId.isNotNull(),
                        settlementItem.payeeId.ne(systemPayeeId),
                        settlementItem.confirmedAt.goe(startAt),
                        settlementItem.confirmedAt.loe(endAt)
                )
                .fetch();
    }

    public List<SettlementItem> findUnsettledItemsByPayeeIds(List<Long> payeeIds, LocalDateTime startAt, LocalDateTime endAt) {
        return queryFactory
                .selectFrom(settlementItem)
                .where(
                        settlementItem.payeeId.in(payeeIds),
                        settlementItem.status.eq(SettlementItemStatus.COLLECTED),
                        settlementItem.confirmedAt.goe(startAt),
                        settlementItem.confirmedAt.loe(endAt)
                )
                .fetch();
    }

    private BooleanExpression statusEq(SettlementStatus status) {
        return status != null ? settlement.status.eq(status) : null;
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        return sellerId != null ? settlement.sellerId.eq(sellerId) : null;
    }

    private BooleanExpression settlementCreatedAtGoe(LocalDateTime startDate) {
        return startDate != null ? settlement.createdAt.goe(startDate) : null;
    }

    private BooleanExpression settlementCreatedAtLoe(LocalDateTime endDate) {
        return endDate != null ? settlement.createdAt.loe(endDate) : null;
    }

    private BooleanExpression payeeIdEq(Long payeeId) {
        return payeeId != null ? settlementItem.payeeId.eq(payeeId) : null;
    }

    private BooleanExpression orderIdEq(Long orderId) {
        return orderId != null ? settlementItem.orderId.eq(orderId) : null;
    }

    private BooleanExpression productIdEq(Long productId) {
        return productId != null ? settlementItem.productId.eq(productId) : null;
    }

    private BooleanExpression itemStatusEq(SettlementItemStatus status) {
        return status != null ? settlementItem.status.eq(status) : null;
    }

    private BooleanExpression confirmedAtGoe(LocalDateTime startDate) {
        return startDate != null ? settlementItem.confirmedAt.goe(startDate) : null;
    }

    private BooleanExpression confirmedAtLoe(LocalDateTime endDate) {
        return endDate != null ? settlementItem.confirmedAt.loe(endDate) : null;
    }
}
