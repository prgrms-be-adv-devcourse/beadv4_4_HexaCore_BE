package com.back.cash.app.usecase;

import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Wallet;
import com.back.cash.dto.response.WalletBalanceResponseDto;
import com.back.common.code.FailureCode;
import com.back.common.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWalletBalanceUseCaseTest {

    @Mock
    private WalletSupport walletSupport;

    @InjectMocks
    private GetWalletBalanceUseCase getWalletBalanceUseCase;

    @Test
    @DisplayName("성공: 유효한 유저 ID로 잔액을 조회하면 올바른 DTO를 반환한다")
    void getWalletBalance() {
        // given
        Long userId = 1L;
        Wallet mockWallet = Wallet.builder()
                .id(100L)
                .userId(userId)
                .balance(BigDecimal.valueOf(5000))
                .build();

        when(walletSupport.findByUserId(userId)).thenReturn(mockWallet);

        // when
        WalletBalanceResponseDto response = getWalletBalanceUseCase.getWalletBalance(userId);

        // then
        assertThat(response.walletId()).isEqualTo(100L);
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(5000));
        verify(walletSupport, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("실패: 지갑이 존재하지 않으면 EntityNotFoundException이 발생한다")
    void getWalletBalance_Fail_NotFound() {
        // given
        Long userId = 999L;
        when(walletSupport.findByUserId(userId))
                .thenThrow(new EntityNotFoundException(FailureCode.WALLET_NOT_FOUND));

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            getWalletBalanceUseCase.getWalletBalance(userId);
        });

        verify(walletSupport, times(1)).findByUserId(userId);
    }
}
