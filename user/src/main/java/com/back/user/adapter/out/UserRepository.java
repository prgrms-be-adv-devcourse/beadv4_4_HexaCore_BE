package com.back.user.adapter.out;

import com.back.user.domain.enums.Provider;
import com.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider authProvider, String providerId);

    boolean existsByNickname(String candidate);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = """
    UPDATE users
    SET
      blind_count = blind_count + 1,
      chat_restricted_until =
        CASE
          WHEN ((blind_count + 1) % :threshold) = 0
            THEN (CAST(:now AS timestamp) + make_interval(secs => :restrictSeconds))
          ELSE chat_restricted_until
        END
    WHERE id = :userId
    """, nativeQuery = true)
    int incrementBlindCount(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("threshold") int threshold,
            @Param("restrictSeconds") long restrictSeconds
    );

    @Query(value = "SELECT chat_restricted_until FROM users WHERE id = :userId", nativeQuery = true)
    LocalDateTime getRestrictedUntil(@Param("userId") Long userId);

}
