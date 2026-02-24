package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.app.MarketSupport;
import com.back.market.domain.MarketUser;
import com.back.market.event.resultpayload.UserUpdatedResultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateMarketUserUseCase {
    private final MarketSupport marketSupport;

    public void updateMarketUser(UserUpdatedResultPayload payload) {
        MarketUser user = marketSupport.findMarketUserById(payload.id());
        if (user != null) {
            user.update(payload.name(), payload.address(), payload.phone());
            log.info("[UpdateMarketUserUseCase] 유저 정보 업데이트 완료 - ID: {}", payload.id());
        } else {
            log.error("[UpdateMarketUserUseCase] 업데이트 대상 유저를 찾을 수 없음 - ID: {}", payload.id());
            throw new CustomException(FailureCode.USER_NOT_FOUND);
        }
    }
}
