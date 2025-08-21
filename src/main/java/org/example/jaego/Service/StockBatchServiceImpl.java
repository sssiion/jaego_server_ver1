package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.stockBatches;
import org.example.jaego.Dto.*;

import org.example.jaego.Exception.InsufficientStockException;
import org.example.jaego.Exception.InvalidQuantityException;
import org.example.jaego.Exception.InventoryNotFoundException;
import org.example.jaego.Exception.StockBatchNotFoundException;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StockBatchServiceImpl implements StockBatchService {


    private final StockBatchRepository stockBatchesRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final StockBatchAutoService stockBatchAutoService;

    @Override
    @Transactional(readOnly = true)
    public List<StockBatchDto> getBatchesByInventoryId(Long inventoryId) {
        List<stockBatches> batches = stockBatchesRepository
                .findByInventoryIdOrderByExpiryDateNullsFirst(inventoryId);

        return batches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentBatchDto> getUrgentBatches(Integer days) {
        LocalDateTime targetDate = LocalDateTime.now().plusDays(days);
        List<stockBatches> batches = stockBatchesRepository.findUrgentBatchesByDays(targetDate);

        return batches.stream()
                .map(batch -> UrgentBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysRemaining((int) java.time.temporal.ChronoUnit.DAYS
                                .between(LocalDate.now(), batch.getExpiryDate()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpiredBatchDto> getExpiredBatches() {
        List<stockBatches> batches = stockBatchesRepository.findExpiredBatches();

        return batches.stream()
                .map(batch -> ExpiredBatchDto.builder()
                        .id(batch.getId())
                        .inventoryId(batch.getInventory().getInventoryId())
                        .inventoryName(batch.getInventory().getName())
                        .categoryName(batch.getInventory().getCategory() != null ?
                                batch.getInventory().getCategory().getCategory() : "미분류")
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .daysExpired((int) java.time.temporal.ChronoUnit.DAYS
                                .between(batch.getExpiryDate(), LocalDate.now()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public StockBatchDto createBatch(StockBatchCreateRequest request) {
        if (!validateBatchData(request)) {
            throw new IllegalArgumentException("유효하지 않은 배치 데이터입니다.");
        }

        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + request.getInventoryId()));

        stockBatches batch = stockBatches.builder()
                .inventory(inventory)
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .build();

        stockBatches savedBatch = stockBatchesRepository.save(batch);

        // 총 수량 업데이트
        inventoryService.updateTotalQuantity(request.getInventoryId());

        return convertToDto(savedBatch);
    }
    @Override
    public void updateBatch(Long batchId, LocalDateTime newExpiryDate, Integer newQuantity) {

        int updatedRows = stockBatchesRepository.updateBatch(batchId, newExpiryDate, newQuantity);


        if (updatedRows == 0) {
            throw new RuntimeException("유통기한 수정 실패 - 배치를 찾을 수 없습니다. ID: " + batchId);
        }
        stockBatches batch = stockBatchesRepository.findById(batchId)
                .orElseThrow(() -> new StockBatchNotFoundException("배치를 찾을 수 없습니다: " + batchId));
        Inventory inventory = inventoryRepository.findInventoriesByStockBatchesContains(batch);
        if ( stockBatchesRepository.existsById(batch.getId())) {
            stockBatchAutoService.adjustBatchesToTotalQuantity(inventory.getInventoryId());
        }
    }


    @Override
    public void deleteBatch(Long batchId) {
        stockBatches batch = stockBatchesRepository.findById(batchId)
                .orElseThrow(() -> new StockBatchNotFoundException("배치를 찾을 수 없습니다: " + batchId));

        Long inventoryId = batch.getInventory().getInventoryId();
        stockBatchesRepository.delete(batch);

        // 총 수량 업데이트
        inventoryService.updateTotalQuantity(inventoryId);
    }
    @Override
    public void SettingBatch(){
        List<Inventory> inventories = inventoryRepository.findInventoriesWithQuantityMismatch();
        inventories.forEach(inventory -> {
            int currentBatchSum = inventory.getStockBatches().stream()
                    .mapToInt(stockBatches::getQuantity)
                    .sum();
            // 4. 부족한 수량을 계산합니다. (목표 수량 - 현재 수량)
            int missingQuantity = inventory.getTotalQuantity() - currentBatchSum;

            // 5. 부족한 수량이 0보다 클 경우에만 새로운 배치를 생성합니다.
            if (missingQuantity > 0) {
                stockBatches nullExpiryBatch = stockBatches.builder()
                        .inventory(inventory)       // 현재 재고와 연결
                        .quantity(missingQuantity)  // 부족한 수량만큼 설정
                        .expiryDate(null)           // 유통기한을 null로 설정
                        .build();
                // 6. 생성된 '유통기한 없음' 배치를 데이터베이스에 저장합니다.
                stockBatchesRepository.save(nullExpiryBatch);
            }

        });

    }

    @Override
    public OperationResult reduceStock(StockReductionRequest request) {
        List<stockBatches> batches = stockBatchesRepository
                .findOldestBatchesByInventoryId(request.getInventoryId());

        int remainingQuantity = request.getQuantity();
        List<String> processedBatches = new ArrayList<>();

        for (stockBatches batch : batches) {
            if (remainingQuantity <= 0) break;

            int availableQuantity = batch.getQuantity();
            int reduceAmount = Math.min(remainingQuantity, availableQuantity);

            int result = stockBatchesRepository.reduceQuantity(batch.getId(), reduceAmount);
            if (result > 0) {
                remainingQuantity -= reduceAmount;
                processedBatches.add("Batch ID: " + batch.getId() + ", 차감량: " + reduceAmount);
            }

        }

        if (remainingQuantity > 0) {
            throw new InsufficientStockException("재고가 부족합니다. 부족한 수량: " + remainingQuantity);
        }

        // 빈 배치 삭제
        stockBatchesRepository.deleteEmptyBatches();


        // 총 수량 업데이트
        inventoryService.updateTotalQuantity(request.getInventoryId());

        return OperationResult.builder()
                .success(true)
                .message("재고 차감이 완료되었습니다.")
                .details(processedBatches)
                .build();
    }

    @Override
    public StockBatchDto addStock(StockAdditionRequest request) {
        return createBatch(StockBatchCreateRequest.builder()
                .inventoryId(request.getInventoryId())
                .quantity(request.getQuantity())
                .expiryDate(request.getExpiryDate())
                .build());
    }

    @Override
    public BatchOperationResult processBatchExpiration() {
        List<stockBatches> expiredBatches = stockBatchesRepository.findExpiredBatches();
        List<String> processedItems = new ArrayList<>();
        int totalExpiredQuantity = 0;

        for (stockBatches batch : expiredBatches) {
            totalExpiredQuantity += batch.getQuantity();
            processedItems.add(String.format("상품: %s, 수량: %d, 만료일: %s",
                    batch.getInventory().getName(),
                    batch.getQuantity(),
                    batch.getExpiryDate()));

            // 만료 배치는 수량을 0으로 설정하거나 삭제 처리
            batch.setQuantity(0);
            stockBatchesRepository.save(batch);

            // 총 수량 업데이트
            inventoryService.updateTotalQuantity(batch.getInventory().getInventoryId());
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
    public BatchStatsDto getBatchStatsByInventory(Long inventoryId) {
        Integer totalQuantity = stockBatchesRepository.getTotalQuantityByInventoryId(inventoryId);
        LocalDateTime earliestExpiry = stockBatchesRepository.getEarliestExpiryDateByInventoryId(inventoryId);
        Long urgentCount = stockBatchesRepository.countUrgentBatchesByDays(LocalDateTime.now().plusDays(7));
        Long expiredCount = stockBatchesRepository.countExpiredBatches();

        return BatchStatsDto.builder()
                .inventoryId(inventoryId)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .earliestExpiryDate(earliestExpiry)
                .urgentBatchCount(urgentCount)
                .expiredBatchCount(expiredCount)
                .build();
    }

    @Override
    public boolean validateBatchData(StockBatchCreateRequest request) {
        if (request.getInventoryId() == null || request.getQuantity() == null) {
            return false;
        }

        if (request.getQuantity() <= 0) {
            return false;
        }

        if (request.getExpiryDate() != null && request.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return inventoryRepository.existsById(request.getInventoryId());
    }

    @Override
    @Transactional(readOnly = true)
    public StockBatchDto getBatchById(Long batchId) {
        stockBatches batch = stockBatchesRepository.findById(batchId)
                .orElseThrow(() -> new StockBatchNotFoundException("배치를 찾을 수 없습니다: " + batchId));

        return convertToDto(batch);
    }
    //  null 배치 선언
    @Override
    public List<StockBatchDto> getnullBatches(){
        return stockBatchesRepository.findNullExpiryBatches().stream().map(r -> convertToDto(r)).collect(Collectors.toList());
    }

    private StockBatchDto convertToDto(stockBatches batch) {
        return StockBatchDto.builder()
                .id(batch.getId())
                .inventoryId(batch.getInventory().getInventoryId())
                .inventoryName(batch.getInventory().getName())
                .quantity(batch.getQuantity())
                .expiryDate(batch.getExpiryDate())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }

}
