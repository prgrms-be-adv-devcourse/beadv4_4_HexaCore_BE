package com.back.market.app;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.usecase.*;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.market.domain.MarketUser;
import com.back.market.event.payload.*;
import com.back.market.event.resultpayload.ProductCreatedResultPayload;
import com.back.market.event.resultpayload.ProductUpdatedResultPayload;
import com.back.market.event.resultpayload.UserCreatedResultPayload;
import com.back.market.event.resultpayload.UserUpdatedResultPayload;
import com.back.market.mapper.MarketProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class MarketInternalFacade {
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final GetSettlementDataUseCase getSettlementDataUseCase;
    private final CreateMarketMemberUseCase createMarketMemberUseCase;
    private final CreateCartUseCase createCartUseCase;
    private final MarketUserRepository userRepository;
    private final MarketProductMapper marketProductMapper;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final UpdateMarketUserUseCase updateMarketUserUseCase;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public boolean confirmPayment(PaymentCompletedRequestDto requestDto) {
        return confirmPaymentUseCase.confirmPayment(requestDto);
    }

    @Transactional
    public void handlePaymentCompletedEvent(PaymentCompletedPayload payload) {
        //log.info("[MarketInternalFacade] 결제 완료 이벤트 처리 시작 - Type: {}, ID: {}", payload.relType(), payload.relId());
        confirmPaymentUseCase.confirmPayment(payload);
    }

    @Transactional
    public void handlePaymentFailedEvent(PaymentFailedPayload payload) {
        //log.info("[MarketInternalFacade] 결제 실패 이벤트 처리 시작 - Type: {}, ID: {}", payload.relType(), payload.relId());
        confirmPaymentUseCase.handlePaymentFailure(payload);
    }

    /**
     * 주문 데이터 조회(feign)
     * @param targetDate 요청 날짜(월단위)
     * @param page 페이지
     * @param size 몇 건이나 보여줄지
     * @return SettlementTargetOrder
     */
    @Transactional(readOnly = true)
    public List<SettlementTargetOrder> getSettlementData(LocalDate targetDate, int page, int size) {
        return getSettlementDataUseCase.getSettlementData(targetDate, page, size);
    }

    /**
     * 회원 가입 이벤트 수신 시 market_member 복제와 cart 생성
     */
    @Transactional
    public void handleUserCreatedEvent(UserCreatedPayload payload) {

        // 이미 존재하는 유저인지 확인 (멱등성 체크)
        if (userRepository.existsById(payload.id())) {
            log.warn("[MarketInternalFacade] 이미 존재하는 유저입니다. UserId: {}", payload.id());
            return;
        }

        // payload의 값을 사용해 이벤트 생성
        UserCreatedResultPayload command = UserCreatedResultPayload.of(
                payload.id(),
                payload.name(),
                payload.email(),
                payload.address(),
                payload.phone()
        );

        // 회원 정보 복제
        MarketUser user = createMarketMemberUseCase.createMarketMember(command);

        // 해당 회원의 장바구니 생성
        if (user != null) {
            createCartUseCase.createCart(user);
        } else {
            log.warn("[MarketInternalFacade] market_member 복제 실패, cart 생성 건너뜀: userId = {}", command.id());
        }
    }

    @Transactional
    public void handleUserUpdatedEvent(UserUpdatedPayload payload) {
        log.info("[MarketInternalFacade] 유저 업데이트 이벤트 처리 시작 - ID: {}", payload.id());
        UserUpdatedResultPayload resultPayload = UserUpdatedResultPayload.of(
                payload.id(), payload.name(), payload.address(), payload.phone()
        );
        updateMarketUserUseCase.updateMarketUser(resultPayload);
    }

    @Transactional
    public void handleProductCreatedEvent(@Valid ProductCreatedPayload payload) {
        try {
            log.info("[MarketInternalFacade] 상품 생성 이벤트 수신, 처리 시작");
            //멱등성 검사는 usecase에서 진행..
            List<ProductCreatedResultPayload> payloads = payload.options().stream()
                    .flatMap(option -> option.values().stream()
                            .map(value -> marketProductMapper.toResultPayload(payload, value))
                    )
                    .toList();

            int savedCount = createProductUseCase.createMarketProduct(payloads);
            if (savedCount > 0) {
                log.info("[MarketInternalFacade] 상품 복제 완료 - 상품명: {}, 처리된 옵션: {}/{}개",
                        payload.productInfo().name(), savedCount, payloads.size());
            } else {
                log.warn("[MarketInternalFacade] 모든 옵션이 이미 존재하여 복제를 건너뜁니다 - 상품명: {}",
                        payload.productInfo().name());
            }
        } catch (Exception e) {
            log.error("[MarketInternalFacade] 상품 처리 중 예외 발생 - 상품명: {}, 사유: {}",
                    payload.productInfo().name(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void handleProductUpdatedEvent(@Valid ProductUpdatedPayload payload) {
        try {
            log.info("[MarketInternalFacade] 상품 수정 이벤트 수신, 처리 시작 - id : {}", payload.productInfo().productInfoId());

            //멱등성 검사는 usecase에서 진행..
            List<ProductUpdatedResultPayload> payloads = payload.options().stream()
                    .flatMap(option -> option.values().stream()
                            .map(value -> marketProductMapper.toResultPayload(payload, value))
                    )
                    .toList();

            int savedCount = updateProductUseCase.updateMarketProduct(payloads);
            if (savedCount > 0) {
                log.info("[MarketInternalFacade] 상품 수정 완료 - 상품명: {}, 처리된 옵션: {}/{}개",
                        payload.productInfo().name(), savedCount, payloads.size());
            } else {
                log.warn("[MarketInternalFacade] 수정 대상 옵션이 없어 수정을 건너뜁니다 - 상품명: {}",
                        payload.productInfo().name());
            }
        } catch (Exception e) {
            log.error("[MarketInternalFacade] 상품 처리 중 예외 발생! - 상품명: {}, 사유: {}",
                    payload.productInfo().name(), e.getMessage(), e);
            throw e;
        }

    }

    @Transactional
    public void handleProductDeletedEvent(@Valid ProductDeletedPayload payload) {
        try {
            log.info("[MarketInternalFacade] 상품 삭제 이벤트 수신, 처리 시작 - id : {}", payload.productInfoId());
            deleteProductUseCase.deleteMarketProduct(payload.productInfoId());
        } catch (Exception e) {
            log.error("[MarketInternalFacade] 상품 삭제 처리 중 예외 발생 - id: {}, 사유: {}",
                    payload.productInfoId(), e.getMessage(), e);
            throw e;
        }
    }
}
