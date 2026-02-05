package com.back.cash.app.usecase;

import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Wallet;
import com.back.cash.dto.response.WalletBalanceResponseDto;
import com.back.cash.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWalletBalanceUseCase {

    private final WalletSupport walletSupport;

    @Transactional(readOnly = true)
    public WalletBalanceResponseDto getWalletBalance(Long userId) {
        Wallet wallet = walletSupport.findByUserId(userId);
        return WalletMapper.toWalletBalanceResponseDto(wallet);
    }
}
