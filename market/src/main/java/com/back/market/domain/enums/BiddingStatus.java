package com.back.market.domain.enums;

public enum BiddingStatus {
    PROCESS,    // 입찰 대기중
    MATCHED,    // 거래 체결
    CANCELLED,  // 입찰 취소
    EXPIRED,    // 입찰 기간 만료(30일)
    PG_PENDING  // 구매 입찰 등록 시 예치금 결제가 필요한 경우
}
