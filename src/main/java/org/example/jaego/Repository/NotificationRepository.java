package org.example.jaego.Repository;

import org.example.jaego.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 대기 중인 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findPendingAlerts();

    // 사용자별 알림 타입 조회
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    // 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId);
}