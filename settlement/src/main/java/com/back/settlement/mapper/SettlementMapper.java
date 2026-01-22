package com.back.settlement.mapper;

import com.back.settlement.app.dto.response.SettlementItemResponse;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.app.dto.response.SettlementResponse;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import com.back.settlement.domain.SettlementLog;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .settlementId(settlement.getId())
                .sellerId(settlement.getSellerId())
                .sellerName(settlement.getSellerName())
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
                .payerId(settlementItem.getPayerId())
                .payeeId(settlementItem.getPayeeId())
                .sellerName(settlementItem.getSellerName())
                .eventType(settlementItem.getEventType())
                .status(settlementItem.getStatus())
                .amount(settlementItem.getAmount())
                .confirmedAt(settlementItem.getConfirmedAt())
                .build();
    }

    public List<SettlementItemResponse> toSettlementItemResponseList(List<SettlementItem> settlementItems) {
        return settlementItems.stream()
                .map(this::toSettlementItemResponse)
                .toList();
    }

    public SettlementLogResponse toSettlementLogResponse(SettlementLog settlementLog) {
        return SettlementLogResponse.builder()
                .logId(settlementLog.getId())
                .settlementId(settlementLog.getSettlement().getId())
                .previousStatus(settlementLog.getPreviousStatus())
                .newStatus(settlementLog.getNewStatus())
                .reason(settlementLog.getReason())
                .actorType(settlementLog.getActorType())
                .actorId(settlementLog.getActorId())
                .createdAt(settlementLog.getCreatedAt())
                .build();
    }

    public List<SettlementLogResponse> toSettlementLogResponseList(List<SettlementLog> settlementLogs) {
        return settlementLogs.stream()
                .map(this::toSettlementLogResponse)
                .toList();
    }
}
