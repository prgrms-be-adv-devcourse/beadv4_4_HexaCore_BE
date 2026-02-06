package com.back.market.adapter.out;

import com.back.market.domain.Order;
import com.back.market.domain.QOrder;
import com.back.market.domain.enums.OrderStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static com.back.market.domain.QBidding.bidding;
import static com.back.market.domain.QMarketProduct.marketProduct;
import static com.back.market.domain.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    /**
     * 구매 내역 조회
     * @param userId 사용자 아이디
     * @param statuses 주문 상태 목록
     * @param pageable 페이지 정보
     * @return Page<Order>
     */
    @Override
    public Page<Order> findBuyHistoryList(Long userId, List<OrderStatus> statuses, Pageable pageable) {

        // 조회
        List<Order> content = queryFactory
                .selectFrom(order)
                .join(order.buyBidding, bidding).fetchJoin()
                .join(bidding.marketProduct, marketProduct).fetchJoin()
                .where(buyBiddingUserIdEq(userId), statusIn(statuses))
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리(fetch 조인 제거로 성능 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .join(order.buyBidding, bidding)
                .where(buyBiddingUserIdEq(userId), statusIn(statuses));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 판매 내역 조회
     * @param userId 사용자 아이디
     * @param statuses 주문 상태 목록
     * @param pageable 페이지 정보
     * @return Page<Order>
     */
    @Override
    public Page<Order> findSellHistoryList(Long userId, List<OrderStatus> statuses, Pageable pageable) {
        // 조회
        List<Order> content = queryFactory
                .selectFrom(order)
                .join(order.sellBidding, bidding).fetchJoin()
                .join(order.buyBidding).fetchJoin()
                .join(order.buyBidding.marketProduct, marketProduct).fetchJoin()
                .where(sellBiddingUserIdEq(userId), statusIn(statuses))
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리(fetch 조인 제거로 성능 최적화)
        JPAQuery<Long> countQuery = queryFactory
                .select(order.count())
                .from(order)
                .join(order.sellBidding, bidding)
                .where(sellBiddingUserIdEq(userId), statusIn(statuses));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 주문 상세 조회
     * @param userId 사용자 아이디
     * @param orderId 주문 아이디
     * @return Optional<Order>
     */
    @Override
    public Optional<Order> findOrderWithDetails(Long userId, Long orderId) {
        Order result = queryFactory
                .selectFrom(order)
                .join(order.buyBidding).fetchJoin()
                .join(order.sellBidding).fetchJoin()
                .join(order.buyBidding.marketProduct, marketProduct).fetchJoin()
                .where(order.id.eq(orderId), isMyOrder(userId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    // BooleanExpressions(검색 조건들)

    // 구매자가 userId와 일치하는지
    private BooleanExpression buyBiddingUserIdEq(Long userId) {
        return userId != null ? order.buyBidding.marketUser.id.eq(userId) : null;
    }

    // 판매자가 userId와 일치하는지
    private BooleanExpression sellBiddingUserIdEq(Long userId) {
        return userId != null ? order.sellBidding.marketUser.id.eq(userId) : null;
    }

    // 상태 리스트 필터링 (statuses에 있는 상태만 조회, HOLD 제외)
    private BooleanExpression statusIn(List<OrderStatus> statuses) {
        return (statuses != null && !statuses.isEmpty()) ? order.orderStatus.in(statuses) : null;
    }

    // 권한 체크: 구매자이거나 판매자인 경우만 허용(기존에 usecase에서 검사하던 내용을 쿼리에서 검증)
    private BooleanExpression isMyOrder(Long userId) {
        if(userId == null) return null;
        return order.buyBidding.marketUser.id.eq(userId)
                .or(order.sellBidding.marketUser.id.eq(userId));
    }

    // OrderSpecifier, 정렬 처리
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if(!pageable.getSort().isEmpty()) {
            for (Sort.Order sortOrder : pageable.getSort()) {
                // createdAt 필드로 요청이 오면 처리
                if ("createdAt".equals(sortOrder.getProperty())) {
                    // 요청이 오름차순/내림차순인지에 따라 처리
                    return sortOrder.isAscending() ? order.createdAt.asc() : order.createdAt.desc();
                }
            }
        }
        return order.createdAt.desc();
    }


}
