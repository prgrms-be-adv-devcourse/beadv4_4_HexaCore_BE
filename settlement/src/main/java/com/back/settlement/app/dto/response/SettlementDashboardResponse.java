package com.back.settlement.app.dto.response;

import java.util.Map;

public record SettlementDashboardResponse(
        long totalCount,
        Map<String, Long> countByStatus
) {}
