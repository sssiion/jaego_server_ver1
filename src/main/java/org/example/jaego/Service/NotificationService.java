package org.example.jaego.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.example.jaego.Dto.*;

public interface NotificationService {

    // 만료 알림 발송
    OperationResult sendExpirationAlert(Long inventoryId, String message, LocalDateTime expiryDate);

    // 사용자 알림 설정 조회
    List<NotificationDto> getUserAlertSettings(Long userId);

    // 정기 알림 스케줄링
    void schedulePeriodicAlerts();
}
