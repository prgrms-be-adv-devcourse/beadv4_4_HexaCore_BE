package com.back.detector.app.usecase;

import com.back.detector.domain.CrawlingBanLevel;
import com.back.detector.domain.CrawlingLogRepository;
import com.back.detector.mapper.CrawlingLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrawlingLogUseCase {

    private final CrawlingLogRepository crawlingLogRepository;

    @Transactional
    public void save(String ip, CrawlingBanLevel banLevel) {
        crawlingLogRepository.save(CrawlingLogMapper.toCrawlingLog(ip, banLevel));
    }
}
