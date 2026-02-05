package com.back.cash.mapper;

import com.back.cash.domain.Wallet;
import com.back.cash.dto.response.WalletBalanceResponseDto;

public class WalletMapper {

    public static WalletBalanceResponseDto toWalletBalanceResponseDto(Wallet wallet) {
        return WalletBalanceResponseDto.builder()
                .walletId(wallet.getId())
                .balance(wallet.getBalance())
                .lastModifiedAt(wallet.getLastModifiedAt())
                .build();
    }
}
