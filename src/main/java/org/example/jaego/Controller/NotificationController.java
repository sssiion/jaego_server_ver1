package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;

import org.example.jaego.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 만료 알림 발송
    @PostMapping("/send-expiry-alert")
    public ResponseEntity<OperationResult> sendExpirationAlert(
            @RequestParam Long inventoryId,
            @RequestParam String message,
            @RequestParam String expiryDate) {

        return ResponseEntity.ok(notificationService.sendExpirationAlert(
                inventoryId, message, LocalDateTime.parse(expiryDate)
        ));
    }

    // 사용자 알림 설정 조회
    @GetMapping("/user/{userId}")
    public List<NotificationDto> getUserAlerts(@PathVariable Long userId) {
        return notificationService.getUserAlertSettings(userId);
    }

    // 정기 알림 스케줄링(수동 트리거)
    @PostMapping("/schedule")
    public ResponseEntity<Void> schedulePeriodicAlerts() {
        notificationService.schedulePeriodicAlerts();
        return ResponseEntity.ok().build();
    }
}
