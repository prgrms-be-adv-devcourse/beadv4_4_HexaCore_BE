package com.back.notification.adapter.out.sqs;

import com.back.notification.app.NotificationSupport;
import com.back.notification.app.NotificationUserSettingUsecase;
import com.back.notification.app.NotificationUserSupport;
import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.NotificationUserSetting;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.NotificationCreatedEvent;
import com.back.notification.dto.PushDispatchMessage;
import com.back.notification.exception.NotificationNotFoundException;
import com.back.notification.mapper.NotificationMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {
    private final SqsTemplate sqsTemplate;

    private final NotificationSupport notificationSupport;
    private final NotificationMapper mapper;
    private final NotificationUserSupport notificationUserSupport;
    private final NotificationUserSettingUsecase notificationUserSettingUsecase;

    @Value("${spring.cloud.aws.sqs.notification-queue}")
    private String queue;

    public void send(NotificationCreatedEvent event) {

        for (String notificationId : event.notificationIds()) {
            try {
                Notification notification =
                        notificationSupport.findById(notificationId);

                NotificationUser user =
                        notificationUserSupport.findById(notification.getUserId());

                if (!isAlertEnabled(user, notification))
                    continue;

                String fcmToken = user.getFcmToken();

                if (fcmToken == null) {
                    log.info("FCM 토큰 없음 - userId={}", notification.getUserId());
                    continue;
                }

                PushDispatchMessage payload =
                        mapper.toPushDispatchMessage(notification, fcmToken);

                sqsTemplate.send(to -> to
                        .queue(queue)
                        .payload(payload)
                );


            } catch (Exception e) {
                log.error(
                        "알림 전송 실패 - notificationId={}, error={}",
                        notificationId,
                        e.getMessage(),
                        e
                );
            }
        }
    }

    private boolean isAlertEnabled(NotificationUser user, Notification notification) {
        try {
            NotificationUserSetting setting = notificationUserSettingUsecase.findById(user.getId());

            Type type = notification.getType();

            boolean isEnabled = switch (type) {
                case BID_COMPLETED, BID_FAILED, PURCHASE_CANCELED -> setting.isBidStatusEnabled();
                case INSPECTION_COMPLETED -> setting.isProductStatusEnabled();
                case PRICE_DROPPED -> setting.isPriceEnabled();
                case SETTLEMENT_COMPLETED -> setting.isSettlementEnabled();
            };

            if (!isEnabled) {
                log.info("푸시 알림 설정 꺼짐 - userId={}, notificationType={}", user.getId(), type);
            }

            return isEnabled;
        } catch (NotificationNotFoundException e) {
            log.warn("알림 설정 없음 - userId={}, 기본값(전송) 사용", user.getId());
            return true;
        } catch (Exception e) {
            log.error("알림 설정 조회 실패 - userId={}, error={}", user.getId(), e.getMessage(), e);
            return true;
        }
    }
}
