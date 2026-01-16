package com.back.user.adapter.out;

import com.back.user.domain.enums.Provider;
import com.back.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider authProvider, String providerId);

    boolean existsByNickname(String candidate);
}
