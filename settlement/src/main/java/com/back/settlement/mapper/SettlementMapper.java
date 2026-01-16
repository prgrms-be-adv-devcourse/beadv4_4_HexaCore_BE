package com.back.settlement.mapper;

import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .settlementId(settlement.getId())
                .sellerId(settlement.getSellerId())
                .status(settlement.getStatus())
                .expectedAt(settlement.getExpectedAt())
                .startAt(settlement.getStartAt())
                .endAt(settlement.getEndAt())
                .completedAt(settlement.getCompletedAt())
                .totalSalesAmount(settlement.getTotalSalesAmount())
                .totalFeeAmount(settlement.getTotalFeeAmount())
                .totalNetAmount(settlement.getTotalNetAmount())
                .build();
    }

    public List<SettlementResponse> toSettlementResponseList(List<Settlement> settlements) {
        return settlements.stream()
                .map(this::toSettlementResponse)
                .toList();
    }

    public SettlementItemResponse toSettlementItemResponse(SettlementItem settlementItem) {
        return SettlementItemResponse.builder()
                .settlementItemId(settlementItem.getId())
                .orderId(settlementItem.getOrderId())
                .productId(settlementItem.getProductId())
                .buyerId(settlementItem.getBuyerId())
                .sellerId(settlementItem.getSellerId())
                .status(settlementItem.getStatus())
                .salesAmount(settlementItem.getSalesAmount())
                .feeAmount(settlementItem.getFeeAmount())
                .netAmount(settlementItem.getNetAmount())
                .transactionAt(settlementItem.getTransactionAt())
                .build();
    }

    public List<SettlementItemResponse> toSettlementItemResponseList(List<SettlementItem> settlementItems) {
        return settlementItems.stream()
                .map(this::toSettlementItemResponse)
                .toList();
    }
}
