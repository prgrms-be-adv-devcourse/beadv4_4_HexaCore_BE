package com.back.detector.app.usecase;

import com.back.detector.domain.HijackLogRepository;
import com.back.detector.mapper.HijackLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class HijackLogUseCase {

    private final HijackLogRepository hijackLogRepository;

    @Transactional
    public void save(Long userId, String userEmail, String currentIp,
                     String existingIps, BigDecimal transactionAmount, String reason) {
        hijackLogRepository.save(HijackLogMapper.toHijackLog(userId, userEmail, currentIp, existingIps, transactionAmount, reason));
    }
}
