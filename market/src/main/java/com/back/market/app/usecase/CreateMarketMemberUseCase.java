package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.MarketSupport;
import com.back.market.domain.MarketUser;
import com.back.market.event.resultpayload.UserCreatedResultPayload;
import com.back.market.mapper.MarketUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateMarketMemberUseCase {
    private final MarketSupport marketSupport;
    private final MarketUserRepository marketUserRepository;
    private final MarketUserMapper marketUserMapper;

    public MarketUser createMarketMember(UserCreatedResultPayload resultPayload) {
        // 기존 회원 존재 여부 확인
        MarketUser existingUser = marketSupport.findMarketUserByIdOptional(resultPayload.id()).orElse(null);

        if (existingUser != null) {
            log.info("[CreateMarketMemberUseCase] 이미 존재하는 회원: id = {}", resultPayload.id());
            return existingUser;
        }

        // 새로운 회원을 MarketUser에 복제
        try {
            log.info("[CreateMarketMemberUseCase] 회원가입 이벤트 수신, 복제 시작: id = {}", resultPayload.id());
            MarketUser newUser = marketUserMapper.toEntity(
                    resultPayload.id(),
                    resultPayload.name(),
                    resultPayload.email(),
                    resultPayload.address(),
                    resultPayload.phone()
            );
            MarketUser savedUser = marketUserRepository.save(newUser);
            log.info("[CreateMarketMemberUseCase] 복제 완료: id = {}", resultPayload.id());
            return savedUser;
        } catch (DataIntegrityViolationException e) {
            // DB 제약조건 위반 시
            log.error("[CreateMarketMemberUseCase] DB 제약 조건 위반으로 복제 실패: id = {}, message = {}", resultPayload.id(), e.getMessage());
            throw new CustomException(FailureCode.CONSTRAINT_VIOLATION);
        } catch (Exception e) {
            log.error("[CreateMarketMemberUseCase] 회원 복제 중 알 수 없는 오류 발생: id = {}, error = {}", resultPayload.id(), e.getClass().getSimpleName());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }

    }

}
