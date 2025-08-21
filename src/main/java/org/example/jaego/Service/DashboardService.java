package org.example.jaego.Service;
import org.example.jaego.Dto.*;



import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {

    // 전체 대시보드 통계 조회
    DashboardStatsDto getDashboardStats();

    // 카테고리별 통계 조회
    List<CategoryStatsDto> getCategoryStats();

    // 임박 상품 통계 조회
    UrgentProductsStatsDto getUrgentProductsStats(Integer days);

    // 만료 추세 데이터 조회
    List<ExpiryTrendDto> getExpiryTrendData(LocalDate startDate, LocalDate endDate);

    List<ExpiryTrendDto> getExpiryTrendData(LocalDateTime startDate, LocalDateTime endDate);

    // 상위 임박 상품 조회
    List<UrgentInventoryDto> getTopUrgentProducts(Integer limit);

    // 재고 회전율 조회
    List<InventoryTurnoverDto> getInventoryTurnoverRate(LocalDate startDate, LocalDate endDate);

    // 월별 만료 리포트 조회
    MonthlyReportDto getMonthlyExpiryReport(int year, int month);

    // 주요 지표 요약
    DashboardSummaryDto getDashboardSummary();
}
