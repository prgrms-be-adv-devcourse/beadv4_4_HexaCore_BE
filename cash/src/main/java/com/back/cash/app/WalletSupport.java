package com.back.cash.app;

import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.WalletType;
import com.back.cash.adapter.out.WalletRepository;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletSupport {
    private final WalletRepository walletRepository;

    // 사용자 지갑 조회
    public Wallet getUserWallet(Long userId) {
        return walletRepository.findByWalletTypeAndUserId(WalletType.USER, userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.WALLET_NOT_FOUND));
    }

    // 시스템 지갑 조회
    public Wallet getSystemWallet() {
        return walletRepository.findByWalletType(WalletType.SYSTEM)
                .orElseThrow(() -> new CustomException(FailureCode.INTERNAL_SERVER_ERROR));
    }

    public Wallet getUserWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.WALLET_NOT_FOUND));
    }

}
