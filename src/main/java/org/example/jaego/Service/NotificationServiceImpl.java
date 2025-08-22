package org.example.jaego.Service;


import org.example.jaego.Entity.Notification;

import org.example.jaego.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public OperationResult sendExpirationAlert(Long inventoryId, String message, LocalDateTime expiryDate) {
        Notification notification = Notification.builder()
                .userId(1234L) // 예시용, 실제 구현 시 로그인 유저 ID
                .type("EXPIRY_ALERT")
                .title("만료 예정 알림")
                .message(message)
                .relatedInventoryId(inventoryId)
                .expiresAt(expiryDate)
                .isSent(true)
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
        return OperationResult.success("만료 알림이 발송되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserAlertSettings(Long userId) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, "EXPIRY_ALERT").stream()
                .map(n -> NotificationDto.builder()
                        .notificationId(n.getNotificationId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .createdAt(n.getCreatedAt())
                        .isRead(n.getIsRead())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void schedulePeriodicAlerts() {
        // 스케줄러 로직 구현 (Spring Scheduler, Quartz 등)
        // 예: 매일 9시에 sendExpirationAlert 호출
    }
}
