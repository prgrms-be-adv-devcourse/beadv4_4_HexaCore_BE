package com.back.notification.app;

import com.back.notification.adapter.out.NotificationRepository;
import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationUser;
import com.back.notification.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class NotificationSupport {
    private final NotificationRepository notificationRepository;

   public Notification findById(String id) {
       return notificationRepository.findById(id)
               .orElseThrow(NotificationNotFoundException::new);
   }

   public List<Notification> findAllById(List<String> ids) {
       List<Notification> result = new ArrayList<>();
       int chunkSize = 1000;
       for (int i = 0; i < ids.size(); i += chunkSize) {
           List<String> chunk = ids.subList(i, Math.min(i + chunkSize, ids.size()));
           result.addAll(notificationRepository.findAllById(chunk));
       }
       return result;
   }

    public Slice<Notification> findRecentNotifications(NotificationUser user, Pageable pageable) {
       return notificationRepository
               .findPageByUserId(pageable, user.getId());
    }
}
