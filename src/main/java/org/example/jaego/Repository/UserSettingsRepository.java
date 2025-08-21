package org.example.jaego.Repository;

import org.example.jaego.Entity.UserSettings;
import org.example.jaego.Entity.stockBatches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    // 사용자별 설정 조회
    Optional<UserSettings> findByUserId(Long userId);

    // 알림 임계값 설정
    @Modifying
    @Query("UPDATE UserSettings us SET us.alertThreshold = :threshold WHERE us.userId = :userId")
    int updateAlertThreshold(@Param("userId") Long userId, @Param("threshold") Integer threshold);

    @Query("SELECT b FROM stockBatches b WHERE b.quantity > 0 AND b.expiryDate > :now AND b.expiryDate < :futureLimit")
    List<stockBatches> findExpiringBatches(@Param("now") LocalDateTime now, @Param("futureLimit") LocalDateTime futureLimit);

    List<stockBatches> findByQuantityGreaterThanAndExpiryDateBetween(Integer quantity, LocalDateTime start, LocalDateTime end);
}