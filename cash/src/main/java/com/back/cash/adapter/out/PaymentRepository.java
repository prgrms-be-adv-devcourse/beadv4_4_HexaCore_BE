package com.back.cash.adapter.out;

import com.back.cash.domain.Payment;
import com.back.cash.domain.enums.RelType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRelTypeAndRelId(RelType relType, Long relId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findWithLockByRelTypeAndRelId(RelType relType, Long relId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findWithLockByTossOrderId(String tossOrderId);
}
