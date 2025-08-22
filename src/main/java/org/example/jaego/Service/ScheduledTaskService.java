package org.example.jaego.Service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Entity.Notification;
import org.example.jaego.Entity.UserSettings;
import org.example.jaego.Entity.stockBatches;
import org.example.jaego.Repository.NotificationRepository;

import org.example.jaego.Repository.StockBatchRepository;
import org.example.jaego.Repository.UserSettingsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskService {

    private final StockBatchRepository stockBatchesRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    @Transactional
    public void checkAndCreateExpiryAlerts() {
        //log.info("유통/소비기한 만료 알림 스케줄러 시작...");
        LocalDateTime now = LocalDateTime.now();
        List<stockBatches> expiringBatches = stockBatchesRepository.findBatchesExpiringBetween(now, now.plusDays(30));

        for (stockBatches batch : expiringBatches) {
            String userId = batch.getInventory().getUserId();
            if (userId == null) continue;

            userSettingsRepository.findByUserId(userId).ifPresent(settings -> {
                if (!settings.isExpiryAlertEnabled()) return;

                LocalDateTime triggerTime = batch.getExpiryDate().minusDays(settings.getAlertThreshold());

                if (now.isAfter(triggerTime) && !notificationRepository.existsByRelatedBatchIdAndType(batch.getId(), "EXPIRY_ALERT")) {
                    createAndSaveNotification(batch, settings);
                }
            });
        }
        //log.info("유통/소비기한 만료 알림 스케줄러 종료.");
    }

    private void createAndSaveNotification(stockBatches batch, UserSettings settings) {
        String term = "소비".equals(batch.getInventory().getCategory().getCategoryType()) ? "소비기한" : "유통기한";
        String title = String.format("'%s' %s 임박 알림", batch.getInventory().getName(), term);
        String message = String.format("보관 중인 '%s'의 %s이 %d일 남았습니다.",
                batch.getInventory().getName(), term, settings.getAlertThreshold());

        Notification notification = Notification.builder()
                .userId(settings.getUserId())
                .type("EXPIRY_ALERT")
                .title(title)
                .message(message)
                .relatedInventoryId(batch.getInventory().getInventoryId())
                .relatedBatchId(batch.getId())
                .build();

        notificationRepository.save(notification);
        log.info("알림 생성: 사용자 ID {}, 배치 ID {}", settings.getUserId(), batch.getId());
    }
}
