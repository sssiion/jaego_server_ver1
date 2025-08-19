package org.example.jaego.Service;

import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.example.jaego.Repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockBatchRepository stockBatchesRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Override
    public DashboardStatsDto getDashboardStats() {
        // 전체 상품 수
        Long totalInventories = inventoryRepository.getTotalInventoryCount();

        // 전체 재고 수량
        List<Object[]> categoryQuantities = stockBatchesRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategoryId() : 0L,
                        Collectors.summingInt(batch -> batch.getQuantity())
                )).entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(Collectors.toList());

        Integer totalQuantity = categoryQuantities.stream()
                .mapToInt(row -> (Integer) row[1])
                .sum();

        // 임박 상품 수 (7일 이내)
        Long urgentProducts = stockBatchesRepository.countUrgentBatchesByDays(LocalDateTime.now().plusDays(7));

        // 만료된 상품 수
        Long expiredProducts = stockBatchesRepository.countExpiredBatches();

        // 카테고리 수
        Long totalCategories = categoryRepository.count();

        return DashboardStatsDto.builder()
                .totalInventories(totalInventories)
                .totalQuantity(totalQuantity)
                .totalCategories(totalCategories)
                .urgentProducts(urgentProducts)
                .expiredProducts(expiredProducts)
                .urgentPercentage(totalInventories > 0 ?
                        (double) urgentProducts / totalInventories * 100 : 0.0)
                .build();
    }

    @Override
    public List<CategoryStatsDto> getCategoryStats() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    Long inventoryCount = categoryRepository.countInventoriesByCategoryId(category.getCategoryId());
                    Integer totalQuantity = stockBatchesRepository.getTotalQuantityByCategory(category.getCategoryId());
                    Long urgentBatchCount = stockBatchesRepository
                            .countUrgentBatchesByCategory(category.getCategoryId(), LocalDateTime.now().plusDays(7));

                    return CategoryStatsDto.builder()
                            .categoryId(category.getCategoryId())
                            .categoryName(category.getCategory())
                            .categoryType(category.getCategoryType())
                            .inventoryCount(inventoryCount)
                            .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                            .urgentBatchCount(urgentBatchCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public UrgentProductsStatsDto getUrgentProductsStats(Integer days) {
        LocalDateTime targetDate = LocalDateTime.now().plusDays(days);

        // 임박 배치 조회
        var urgentBatches = stockBatchesRepository.findUrgentBatchesByDays(targetDate);

        // 카테고리별 임박 상품 수
        var categoryUrgentCount = urgentBatches.stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류",
                        Collectors.counting()
                ));

        // 총 임박 수량
        Integer totalUrgentQuantity = urgentBatches.stream()
                .mapToInt(batch -> batch.getQuantity())
                .sum();

        // 임박 상품 목록 (상위 10개)
        List<UrgentInventoryDto> topUrgentProducts = getTopUrgentProducts(10);

        return UrgentProductsStatsDto.builder()
                .totalUrgentBatches((long) urgentBatches.size())
                .totalUrgentQuantity(totalUrgentQuantity)
                .categoryUrgentCount(categoryUrgentCount)
                .topUrgentProducts(topUrgentProducts)
                .targetDays(days)
                .build();
    }

    @Override
    public List<ExpiryTrendDto> getExpiryTrendData(LocalDate startDate, LocalDate endDate) {
        List<ExpiryTrendDto> trendData = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate nextDate = currentDate.plusDays(1);

            // 해당 날짜에 만료되는 배치들 조회
            var batchesExpiringOnDate = stockBatchesRepository
                    .findBatchesExpiringBetween(currentDate, currentDate);

            Integer expiringQuantity = batchesExpiringOnDate.stream()
                    .mapToInt(batch -> batch.getQuantity())
                    .sum();

            trendData.add(ExpiryTrendDto.builder()
                    .date(currentDate)
                    .expiringBatchCount((long) batchesExpiringOnDate.size())
                    .expiringQuantity(expiringQuantity)
                    .build());

            currentDate = nextDate;
        }

        return trendData;
    }

    @Override
    public List<UrgentInventoryDto> getTopUrgentProducts(Integer limit) {
        return inventoryRepository.findInventoriesWithUrgentBatches(7).stream()
                .limit(limit)
                .map(inventory -> {
                    LocalDateTime earliestExpiry = stockBatchesRepository
                            .getEarliestExpiryDateByInventoryId(inventory.getInventoryId());

                    return UrgentInventoryDto.builder()
                            .inventoryId(inventory.getInventoryId())
                            .name(inventory.getName())
                            .categoryName(inventory.getCategory() != null ?
                                    inventory.getCategory().getCategory() : "미분류")
                            .totalQuantity(inventory.getTotalQuantity())
                            .earliestExpiryDate(earliestExpiry)
                            .daysRemaining(earliestExpiry != null ?
                                    (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), earliestExpiry) : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryTurnoverDto> getInventoryTurnoverRate(LocalDate startDate, LocalDate endDate) {
        return inventoryRepository.findAll().stream()
                .map(inventory -> {
                    // 기간 내 총 출고량
                    Integer totalOutbound = stockMovementRepository
                            .getTotalOutboundByInventoryAndPeriod(
                                    inventory.getInventoryId(),
                                    startDate.atStartOfDay(),
                                    endDate.atStartOfDay());

                    // 평균 재고량 (간단히 현재 재고로 계산)
                    Integer avgInventory = inventory.getTotalQuantity();

                    // 회전율 계산
                    Double turnoverRate = avgInventory > 0 ?
                            (double) totalOutbound / avgInventory : 0.0;

                    return InventoryTurnoverDto.builder()
                            .inventoryId(inventory.getInventoryId())
                            .inventoryName(inventory.getName())
                            .categoryName(inventory.getCategory() != null ?
                                    inventory.getCategory().getCategory() : "미분류")
                            .totalOutbound(totalOutbound != null ? totalOutbound : 0)
                            .averageInventory(avgInventory)
                            .turnoverRate(turnoverRate)
                            .build();
                })
                .filter(dto -> dto.getTurnoverRate() > 0)
                .sorted((a, b) -> Double.compare(b.getTurnoverRate(), a.getTurnoverRate()))
                .collect(Collectors.toList());
    }

    @Override
    public MonthlyReportDto getMonthlyExpiryReport(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 월에 만료된 배치들
        var expiredBatches = stockBatchesRepository.findBatchesExpiringBetween(startDate, endDate);

        Integer totalExpiredQuantity = expiredBatches.stream()
                .mapToInt(batch -> batch.getQuantity())
                .sum();

        // 카테고리별 만료 통계
        var categoryExpiredStats = expiredBatches.stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류",
                        Collectors.summingInt(batch -> batch.getQuantity())
                ));

        // 일별 만료 추세
        List<ExpiryTrendDto> dailyTrend = getExpiryTrendData(startDate, endDate);

        return MonthlyReportDto.builder()
                .year(year)
                .month(month)
                .totalExpiredBatches((long) expiredBatches.size())
                .totalExpiredQuantity(totalExpiredQuantity)
                .categoryExpiredStats(categoryExpiredStats)
                .dailyExpiryTrend(dailyTrend)
                .build();
    }

    @Override
    public DashboardSummaryDto getDashboardSummary() {
        DashboardStatsDto stats = getDashboardStats();
        List<UrgentInventoryDto> topUrgent = getTopUrgentProducts(5);

        // 이번 주 만료 예정 수량
        LocalDateTime weekEnd = LocalDateTime.now().plusDays(7);
        Long thisWeekExpiring = stockBatchesRepository.countUrgentBatchesByDays(weekEnd);

        // 이번 달 만료 예정 수량
        LocalDateTime monthEnd = LocalDateTime.now().plusDays(30);
        Long thisMonthExpiring = stockBatchesRepository.countUrgentBatchesByDays(monthEnd);

        return DashboardSummaryDto.builder()
                .totalInventories(stats.getTotalInventories())
                .totalQuantity(stats.getTotalQuantity())
                .urgentProducts(stats.getUrgentProducts())
                .expiredProducts(stats.getExpiredProducts())
                .thisWeekExpiring(thisWeekExpiring)
                .thisMonthExpiring(thisMonthExpiring)
                .topUrgentProducts(topUrgent)
                .lastUpdated(java.time.LocalDateTime.now())
                .build();
    }
}
