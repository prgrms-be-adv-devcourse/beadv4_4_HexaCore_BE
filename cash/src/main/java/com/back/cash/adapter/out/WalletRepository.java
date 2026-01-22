package com.back.cash.adapter.out;

import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.WalletType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByWalletTypeAndUserId(WalletType walletType, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByWalletType(WalletType walletType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserId(Long userId);
}
