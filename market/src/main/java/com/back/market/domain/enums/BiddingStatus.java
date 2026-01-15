package com.back.market.domain.enums;

public enum BiddingStatus {
    PROCESS,    // 입찰 대기중
    MATCHED,    // 거래 체결
    CANCELLED,  // 입찰 취소
    EXPIRED     // 입찰 기간 만료(30일)
}
