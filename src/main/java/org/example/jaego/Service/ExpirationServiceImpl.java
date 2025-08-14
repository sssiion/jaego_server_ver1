package org.example.jaego.Service;


import org.example.jaego.Entity.stockBatches;

import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpirationServiceImpl implements ExpirationService {

    @Autowired
    private StockBatchRepository stockBatchesRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<UrgentInventoryDto> getUrgentProductsByType(String categoryType, Integer days) {
        // 해당 타입의 카테고리들 조회
        var categories = categoryRepository.findByCategoryTypeOrderByCategory(categoryType);
        var categoryIds = categories.stream()
                .map(category -> category.getCategoryId())
                .collect(Collectors.toList());

        return inventoryRepository.findInventoriesWithUrgentBatches(days).stream()
                .filter(inventory -> inventory.getCategory() != null &&
                        categoryIds.contains(inventory.getCategory().getCategoryId()))
                .map(inventory -> {
                    LocalDate earliestExpiry = stockBatchesRepository
                            .getEarliestExpiryDateByInventoryId(inventory.getInventoryId());

                    return UrgentInventoryDto.builder()
                            .inventoryId(inventory.getInventoryId())
                            .name(inventory.getName())
                            .categoryName(inventory.getCategory().getCategory())
                            .totalQuantity(inventory.getTotalQuantity())
                            .earliestExpiryDate(earliestExpiry)
                            .daysRemaining(calculateDaysRemaining(earliestExpiry))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentInventoryDto> searchUrgentProducts(String keyword, Integer days) {
        return inventoryRepository.findInventoriesWithUrgentBatches(days).stream()
                .filter(inventory -> inventory.getName().toLowerCase()
                        .contains(keyword.toLowerCase()))
                .map(inventory -> {
                    LocalDate earliestExpiry = stockBatchesRepository
                            .getEarliestExpiryDateByInventoryId(inventory.getInventoryId());

                    return UrgentInventoryDto.builder()
                            .inventoryId(inventory.getInventoryId())
                            .name(inventory.getName())
                            .categoryName(inventory.getCategory() != null ?
                                    inventory.getCategory().getCategory() : "미분류")
                            .totalQuantity(inventory.getTotalQuantity())
                            .earliestExpiryDate(earliestExpiry)
                            .daysRemaining(calculateDaysRemaining(earliestExpiry))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiredBatchDto> getExpiringToday() {
        LocalDate today = LocalDate.now();
        var batchesToday = stockBatchesRepository.findBatchesExpiringBetween(today, today);

        return batchesToday.stream()
                .map(batch -> ExpiredBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysExpired(0) // 오늘 만료
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentBatchDto> getExpiringThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);

        var batchesThisWeek = stockBatchesRepository.findUrgentBatchesByDays(weekEnd);

        return batchesThisWeek.stream()
                .map(batch -> UrgentBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysRemaining(calculateDaysRemaining(batch.getExpiryDate()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentBatchDto> getExpiringThisMonth() {
        LocalDate today = LocalDate.now();
        LocalDate monthEnd = today.plusDays(30);

        var batchesThisMonth = stockBatchesRepository.findUrgentBatchesByDays(monthEnd);

        return batchesThisMonth.stream()
                .map(batch -> UrgentBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysRemaining(calculateDaysRemaining(batch.getExpiryDate()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Integer calculateDaysRemaining(LocalDate expiryDate) {
        if (expiryDate == null) return null;

        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return (int) days;
    }

    @Override
    public OperationResult sendExpirationAlerts(Integer days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        var urgentBatches = stockBatchesRepository.findUrgentBatchesByDays(targetDate);

        List<String> sentAlerts = new ArrayList<>();
        int successCount = 0;

        for (stockBatches batch : urgentBatches) {
            try {
                // 만료 알림 생성 및 발송
                String alertMessage = String.format("상품 '%s'의 배치가 %d일 후 만료됩니다. (수량: %d)",
                        batch.getInventory().getName(),
                        calculateDaysRemaining(batch.getExpiryDate()),
                        batch.getQuantity());

                // 알림 서비스를 통해 발송 (구현 예정)
                notificationService.sendExpirationAlert(
                        batch.getInventory().getInventoryId(),
                        alertMessage,
                        batch.getExpiryDate().atStartOfDay());

                sentAlerts.add(alertMessage);
                successCount++;

            } catch (Exception e) {
                sentAlerts.add("발송 실패: " + batch.getInventory().getName() + " - " + e.getMessage());
            }
        }

        return OperationResult.builder()
                .success(successCount > 0)
                .message(String.format("총 %d개 알림 중 %d개 발송 완료", urgentBatches.size(), successCount))
                .details(sentAlerts)
                .build();
    }

    @Override
    public BatchOperationResult processExpiredProducts() {
        var expiredBatches = stockBatchesRepository.findExpiredBatches();
        List<String> processedItems = new ArrayList<>();
        int totalExpiredQuantity = 0;

        for (stockBatches batch : expiredBatches) {
            totalExpiredQuantity += batch.getQuantity();

            processedItems.add(String.format("상품: %s, 만료 수량: %d, 만료일: %s",
                    batch.getInventory().getName(),
                    batch.getQuantity(),
                    batch.getExpiryDate()));

            // 만료 처리 로직 (수량을 0으로 설정하거나 별도 테이블로 이관)
            batch.setQuantity(0);
            stockBatchesRepository.save(batch);
        }

        // 빈 배치 삭제
        stockBatchesRepository.deleteEmptyBatches();

        return BatchOperationResult.builder()
                .totalProcessed(expiredBatches.size())
                .totalQuantity(totalExpiredQuantity)
                .processedItems(processedItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpirationRiskDto getProductsByRiskLevel() {
        LocalDate today = LocalDate.now();

        // 고위험: 3일 이내
        var highRisk = stockBatchesRepository.findUrgentBatchesByDays(today.plusDays(3));

        // 중위험: 7일 이내
        var mediumRisk = stockBatchesRepository.findUrgentBatchesByDays(today.plusDays(7)).stream()
                .filter(batch -> calculateDaysRemaining(batch.getExpiryDate()) > 3)
                .collect(Collectors.toList());

        // 저위험: 14일 이내
        var lowRisk = stockBatchesRepository.findUrgentBatchesByDays(today.plusDays(14)).stream()
                .filter(batch -> calculateDaysRemaining(batch.getExpiryDate()) > 7)
                .collect(Collectors.toList());

        return ExpirationRiskDto.builder()
                .highRiskCount((long) highRisk.size())
                .mediumRiskCount((long) mediumRisk.size())
                .lowRiskCount((long) lowRisk.size())
                .highRiskProducts(convertToUrgentBatchDtos(highRisk))
                .mediumRiskProducts(convertToUrgentBatchDtos(mediumRisk))
                .lowRiskProducts(convertToUrgentBatchDtos(lowRisk))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentBatchDto> getExpiringInPeriod(LocalDate startDate, LocalDate endDate) {
        var batches = stockBatchesRepository.findBatchesExpiringBetween(startDate, endDate);

        return convertToUrgentBatchDtos(batches);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpirationStatsDto getExpirationStats(Integer days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);

        // 임박 배치들
        var urgentBatches = stockBatchesRepository.findUrgentBatchesByDays(targetDate);

        // 카테고리별 임박 수량
        var categoryStats = urgentBatches.stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류",
                        Collectors.summingInt(batch -> batch.getQuantity())
                ));

        // 총 임박 수량
        Integer totalUrgentQuantity = urgentBatches.stream()
                .mapToInt(batch -> batch.getQuantity())
                .sum();

        // 이미 만료된 수량
        var expiredBatches = stockBatchesRepository.findExpiredBatches();
        Integer totalExpiredQuantity = expiredBatches.stream()
                .mapToInt(batch -> batch.getQuantity())
                .sum();

        return ExpirationStatsDto.builder()
                .targetDays(days)
                .totalUrgentBatches((long) urgentBatches.size())
                .totalUrgentQuantity(totalUrgentQuantity)
                .totalExpiredBatches((long) expiredBatches.size())
                .totalExpiredQuantity(totalExpiredQuantity)
                .categoryUrgentStats(categoryStats)
                .build();
    }

    private List<UrgentBatchDto> convertToUrgentBatchDtos(List<stockBatches> batches) {
        return batches.stream()
                .map(batch -> UrgentBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysRemaining(calculateDaysRemaining(batch.getExpiryDate()))
                        .build())
                .collect(Collectors.toList());
    }
}
