package com.back.user.adapter.out;

import com.back.user.domain.enums.Provider;
import com.back.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider authProvider, String providerId);

    boolean existsByNickname(String candidate);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    @Query(value = """
    UPDATE users
    SET
      blind_count = blind_count + 1,
      chat_restricted_until =
        CASE
          WHEN ((blind_count + 1) % :threshold) = 0
            THEN :now + (:restrictSeconds || ' seconds')::interval
          ELSE chat_restricted_until
        END
    WHERE id = :userId
    RETURNING chat_restricted_until
    """, nativeQuery = true)
    LocalDateTime incrementBlindAndReturnRestrictedUntil(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("threshold") int threshold,
            @Param("restrictSeconds") long restrictSeconds
    );
}
