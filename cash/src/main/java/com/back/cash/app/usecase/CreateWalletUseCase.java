package com.back.cash.app.usecase;

import com.back.cash.adapter.out.WalletRepository;
import com.back.cash.domain.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateWalletUseCase {

    private final WalletRepository walletRepository;

    @Transactional
    public void createWallet(Long userId) {

        if (walletRepository.existsByUserId(userId)) {
            log.info("지갑이 이미 존재합니다. 생성을 건너뜁니다: userId={}", userId);
            return;
        }

        try {
            Wallet wallet = Wallet.builder()
                    .userId(userId)
                    .balance(BigDecimal.ZERO)
                    .build();

            walletRepository.save(wallet);
            log.info("지갑 생성 성공: userId={}", userId);

        } catch (DataIntegrityViolationException e) {
            log.info("동시성 이슈로 인한 중복 지갑 생성 방지: userId={}", userId);
        }
    }
}
