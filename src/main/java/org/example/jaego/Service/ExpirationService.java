package org.example.jaego.Service;

import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.util.List;

public interface ExpirationService {

    // 타입별 임박 상품 조회 (유통기한/소비기한)
    List<UrgentInventoryDto> getUrgentProductsByType(String categoryType, Integer days);

    // 임박 상품 검색
    List<UrgentInventoryDto> searchUrgentProducts(String keyword, Integer days);

    // 오늘 만료 상품 조회
    List<ExpiredBatchDto> getExpiringToday();

    // 이번 주 만료 상품 조회
    List<UrgentBatchDto> getExpiringThisWeek();

    // 이번 달 만료 상품 조회
    List<UrgentBatchDto> getExpiringThisMonth();

    // 남은 일수 계산
    Integer calculateDaysRemaining(LocalDate expiryDate);

    // 만료 알림 발송
    OperationResult sendExpirationAlerts(Integer days);

    // 만료 상품 처리
    BatchOperationResult processExpiredProducts();

    // 만료 위험도별 상품 조회
    ExpirationRiskDto getProductsByRiskLevel();

    // 특정 기간 만료 상품 조회
    List<UrgentBatchDto> getExpiringInPeriod(LocalDate startDate, LocalDate endDate);

    // 만료 통계 조회
    ExpirationStatsDto getExpirationStats(Integer days);
}
