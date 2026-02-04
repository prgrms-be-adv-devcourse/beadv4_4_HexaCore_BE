package com.back.market.app.usecase;

import com.back.common.user.event.UserCreatedEvent;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import com.back.market.mapper.MarketUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateMarketMemberUseCase {
    private final MarketUserRepository marketUserRepository;
    private final MarketUserMapper marketUserMapper;

    @Transactional
    public void createMarketMember(UserCreatedEvent event) {
        if (marketUserRepository.existsById(event.id())) {
            log.info("[Market] 이미 존재하는 회원 데이터: id = {}", event.id());
            // TODO 업데이트 로직 추가하기
            return;
        }

        if (event.id() == null || event.email() == null) {
            log.error("UserCreatedEvent의 id 또는 email이 null입니다. event={}", event);
            return;
        }

        MarketUser marketUser = marketUserMapper.toEntity(
                event.id(),
                event.nickname(),
                event.email(),
                event.address(),
                event.phone(),
                event.profileImageUrl()
        );

        marketUserRepository.save(marketUser);
        log.info("[CreateMarketMemberUseCase] 회원 데이터 복제 완료: id = {}", event.id());
    }

}
