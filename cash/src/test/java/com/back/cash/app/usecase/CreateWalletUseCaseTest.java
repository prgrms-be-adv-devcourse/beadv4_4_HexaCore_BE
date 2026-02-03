package com.back.cash.app.usecase;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CreateWalletUseCaseTest {

    @Autowired CreateWalletUseCase createWalletUseCase;
    @Autowired EntityManager em;

    @Test
    @DisplayName("이미 지갑이 존재하는 사용자가 지갑 생성을 다시 요청해도, 추가로 지갑이 생성되지 않는다 (멱등성 검증)")
    void createWallet_is_idempotent() {
        Long userId = 1L;

        createWalletUseCase.createWallet(userId);
        createWalletUseCase.createWallet(userId);

        em.flush();

        Long cnt = em.createQuery(
                        "select count(w) from Wallet w where w.userId = :userId",
                        Long.class
                ).setParameter("userId", userId)
                .getSingleResult();

        assertThat(cnt).isEqualTo(1L);
    }
}
