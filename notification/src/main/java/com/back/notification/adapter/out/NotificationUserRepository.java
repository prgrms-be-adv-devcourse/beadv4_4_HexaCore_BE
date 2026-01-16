package com.back.notification.adapter.out;

import com.back.notification.domain.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {

    @Query("select u.fcmToken from NotificationUser u where u.id = :id")
    Optional<String> findFcmTokenByUserId(Long userId);
}
