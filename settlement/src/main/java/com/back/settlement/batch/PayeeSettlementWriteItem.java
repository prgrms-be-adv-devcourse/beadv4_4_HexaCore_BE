package com.back.settlement.batch;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PayeeSettlementWriteItem {
    private final Long payeeId;
    private Settlement settlement;
    private List<SettlementItem> items;

    private PayeeSettlementWriteItem(Long payeeId) {
        this.payeeId = payeeId;
    }

    public static PayeeSettlementWriteItem create(Long payeeId) {
        return new PayeeSettlementWriteItem(payeeId);
    }

    public void enrich(Settlement settlement, List<SettlementItem> items) {
        this.settlement = settlement;
        this.items = items;
    }

    public boolean isEnriched() {
        return settlement != null;
    }
}
