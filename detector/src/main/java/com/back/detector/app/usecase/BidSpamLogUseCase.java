package com.back.detector.app.usecase;

import com.back.detector.domain.BidSpamBanLevel;
import com.back.detector.domain.BidSpamLogRepository;
import com.back.detector.dto.response.BidSpamLogResponse;
import com.back.detector.mapper.BidSpamLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BidSpamLogUseCase {

    private final BidSpamLogRepository bidSpamLogRepository;
    private final BidSpamLogMapper bidSpamLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long userId, BidSpamBanLevel banLevel, Long requestCount) {
        bidSpamLogRepository.save(bidSpamLogMapper.toBidSpamLog(userId, banLevel, requestCount));
    }

    @Transactional(readOnly = true)
    public Page<BidSpamLogResponse> findPageByCreatedAtDesc(Pageable pageable) {
        return bidSpamLogRepository.findAll(pageable)
                .map(BidSpamLogResponse::from);
    }
}
