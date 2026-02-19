package com.back.detector.mapper;

import com.back.detector.domain.HijackLog;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HijackLogMapper {

    public HijackLog toHijackLog(Long userId, String userEmail, String currentIp,
                                 String existingIps, BigDecimal transactionAmount, String reason) {
        return HijackLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .currentIp(currentIp)
                .existingIps(existingIps)
                .transactionAmount(transactionAmount)
                .reason(reason)
                .build();
    }
}
