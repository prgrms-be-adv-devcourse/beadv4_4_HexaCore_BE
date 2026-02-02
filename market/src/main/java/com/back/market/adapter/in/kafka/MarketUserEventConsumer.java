package com.back.market.adapter.in.kafka;

import com.back.common.market.event.UserCreatedEvent;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.MarketUser;
import com.back.market.mapper.MarketUserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketUserEventConsumer {

    private final MarketUserRepository marketUserRepository;
    private final MarketUserMapper marketUserMapper;

    @Transactional
    @KafkaListener(
            topics = "UserCreatedEvent",
            groupId = "resello-market-group"
    )
    public void consume(UserCreatedEvent event) {
        log.info("[Market] UserCreatedEvent 수신: {}", event.id());

        if (marketUserRepository.existsById(event.id())) {
            log.info("[Market] 이미 존재하는 회원 데이터: id = {}", event.id());
            // TODO 업데이트 로직 추가하기
            return;
        }

        if (event.id() == null || event.email() == null) {
            log.error("UserCreatedEvent의 id 또는 email이 null입니다. event={}", event);
            return;
        }

        try {
            MarketUser marketUser = marketUserMapper.toEntity(
                    event.id(),
                    event.nickname(),
                    event.email(),
                    event.address(),
                    event.phone(),
                    event.profileImageUrl()
            );

            marketUserRepository.save(marketUser);
            log.info("[Market] 회원 데이터 복제 완료: id = {}", event.id());
        } catch (DataIntegrityViolationException e) {
            log.error("[Market] 영구적 데이터 에러(스킵) id = {}, error = {}", event.id(), e.getMessage());
        } catch (Exception e) {
            //일시적 오류일 경우
            log.error("[Market] 일시적 오류(재시도): id = {}, error = {}", event.id(), e.getMessage());
            throw e;
        }
    }

}
