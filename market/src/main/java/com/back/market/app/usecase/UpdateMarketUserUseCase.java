package com.back.market.app.usecase;

import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import com.back.market.event.resultpayload.UserUpdatedResultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateMarketUserUseCase {
    private final MarketUserRepository marketUserRepository;

    public void updateMarketUser(UserUpdatedResultPayload payload) {
        MarketUser user = marketUserRepository.findById(payload.id()).orElse(null);
        if (user != null) {
            user.update(payload.name(), payload.address(), payload.phone());
            log.info("[UpdateMarketUserUseCase] 유저 정보 업데이트 완료 - ID: {}", payload.id());
        } else {
            log.warn("[UpdateMarketUserUseCase] 업데이트 대상 유저를 찾을 수 없음 - ID: {}", payload.id());
        }
    }
}
