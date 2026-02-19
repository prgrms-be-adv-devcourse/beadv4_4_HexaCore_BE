package com.back.detector.app.usecase;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingLogRepository;
import com.back.detector.dto.response.CrawlingLogResponse;
import com.back.detector.mapper.CrawlingLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrawlingLogUseCase {

    private final CrawlingLogRepository crawlingLogRepository;
    private final CrawlingLogMapper crawlingLogMapper;

    @Transactional
    public void save(String ip, int requestCount, CrawlingBanLevel banLevel) {
        crawlingLogRepository.save(crawlingLogMapper.toCrawlingLog(ip, requestCount, banLevel));
    }

    @Transactional(readOnly = true)
    public Page<CrawlingLogResponse> findPageByCreatedAtDesc(Pageable pageable) {
        return crawlingLogRepository.findAll(pageable)
                .map(CrawlingLogResponse::from);
    }
}
