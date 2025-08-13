package org.example.jaego.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.Stock_Batches;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StockBatchAutoService {

    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository stock_BatchesRepository;

    /**
     * 총수량에 맞춰 배치 자동 조정
     * - 총수량 > 배치 합계: null 배치 생성/추가
     * - 총수량 < 배치 합계: FIFO로 배치 차감
     * - 총수량 = 0이어도 Inventory는 절대 삭제하지 않음
     */
    public void adjustBatchesToTotalQuantity(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("재고를 찾을 수 없습니다: ID " + inventoryId));

        // 현재 모든 배치의 수량 합계 계산
        Integer currentBatchTotal = stock_BatchesRepository.calculateTotalQuantityByInventoryId(inventoryId);
        currentBatchTotal = currentBatchTotal != null ? currentBatchTotal : 0;

        Integer targetTotal = inventory.getTotalQuantity();
        Integer difference = targetTotal - currentBatchTotal;

        log.info("배치 조정 시작 - 재고 ID: {}, 상품명: '{}', 목표 총수량: {}, 현재 배치 합계: {}, 차이: {}",
                inventoryId, inventory.getName(), targetTotal, currentBatchTotal, difference);

        if (difference > 0) {
            // 부족한 수량만큼 null 배치 생성/추가
            addNullBatchForDifference(inventory, difference);
            log.info("null 배치 {}개 추가 완료", difference);

        } else if (difference < 0) {
            // 초과 수량만큼 배치에서 FIFO 차감
            reduceBatchesForDifference(inventoryId, Math.abs(difference));
            log.info("배치에서 {}개 차감 완료", Math.abs(difference));

        } else {
            log.info("배치 수량이 이미 정확함 - 조정 불필요");
        }

        // 총수량이 0이어도 Inventory는 보존
        if (targetTotal == 0) {
            log.info("⚠️ 상품 '{}' 총수량이 0이지만 상품 정보는 보존됩니다", inventory.getName());
        }

        log.info("배치 조정 완료 - 재고 ID: {}", inventoryId);
    }

    /**
     * 부족한 수량만큼 null 배치 생성/추가
     */
    private void addNullBatchForDifference(Inventory inventory, Integer addQuantity) {
        // 기존 null 배치 찾기
        List<Stock_Batches> existingNullBatches = stock_BatchesRepository
                .findNullExpiryBatchesByInventoryId(inventory.getInventoryId());

        if (!existingNullBatches.isEmpty()) {
            // 기존 null 배치에 수량 추가 (첫 번째 배치에 추가)
            Stock_Batches existingNullBatch = existingNullBatches.get(0);
            existingNullBatch.setQuantity(existingNullBatch.getQuantity() + addQuantity);
            stock_BatchesRepository.save(existingNullBatch);

            log.debug("기존 null 배치에 {}개 추가 - 배치 ID: {}", addQuantity, existingNullBatch.getId());

        } else {
            // 새로운 null 배치 생성
            Stock_Batches newNullBatch = Stock_Batches.builder()
                    .inventory(inventory)
                    .quantity(addQuantity)
                    .expiryDate(null) // null 유통기한
                    .build();

            stock_BatchesRepository.save(newNullBatch);
            log.debug("새로운 null 배치 {}개 생성 - 배치 ID: {}", addQuantity, newNullBatch.getId());
        }
    }

    /**
     * 초과 수량만큼 배치에서 FIFO 차감
     * 1단계: null 배치부터 차감
     * 2단계: 유통기한 있는 배치를 유통기한 순으로 차감
     */
    private void reduceBatchesForDifference(Long inventoryId, Integer reduceQuantity) {
        int remainingToReduce = reduceQuantity;

        // 1단계: null 배치부터 차감
        List<Stock_Batches> nullBatches = stock_BatchesRepository
                .findNullExpiryBatchesByInventoryId(inventoryId);

        List<Stock_Batches> batchesToUpdate = new ArrayList<>();
        List<Stock_Batches> batchesToDelete = new ArrayList<>();

        for (Stock_Batches nullBatch : nullBatches) {
            if (remainingToReduce <= 0) break;

            if (nullBatch.getQuantity() <= remainingToReduce) {
                // null 배치 전체 삭제
                remainingToReduce -= nullBatch.getQuantity();
                batchesToDelete.add(nullBatch);
                log.debug("null 배치 {}개 전체 삭제 - 배치 ID: {}", nullBatch.getQuantity(), nullBatch.getId());

            } else {
                // null 배치 일부 차감
                nullBatch.setQuantity(nullBatch.getQuantity() - remainingToReduce);
                batchesToUpdate.add(nullBatch);
                log.debug("null 배치에서 {}개 차감 - 배치 ID: {}, 남은 수량: {}",
                        remainingToReduce, nullBatch.getId(), nullBatch.getQuantity());
                remainingToReduce = 0;
            }
        }

        // 2단계: null 배치로 부족하면 유통기한 있는 배치에서 FIFO 차감
        if (remainingToReduce > 0) {
            List<Stock_Batches> expiryBatches = stock_BatchesRepository
                    .findExpiryBatchesByInventoryId(inventoryId);

            for (Stock_Batches batch : expiryBatches) {
                if (remainingToReduce <= 0) break;

                if (batch.getQuantity() <= remainingToReduce) {
                    // 배치 전체 삭제
                    remainingToReduce -= batch.getQuantity();
                    batchesToDelete.add(batch);
                    log.debug("유통기한 배치({}) {}개 전체 삭제 - 배치 ID: {}",
                            batch.getExpiryDate(), batch.getQuantity(), batch.getId());

                } else {
                    // 배치 일부 차감
                    batch.setQuantity(batch.getQuantity() - remainingToReduce);
                    batchesToUpdate.add(batch);
                    log.debug("유통기한 배치({})에서 {}개 차감 - 배치 ID: {}, 남은 수량: {}",
                            batch.getExpiryDate(), remainingToReduce, batch.getId(), batch.getQuantity());
                    remainingToReduce = 0;
                }
            }
        }

        // 배치 업데이트 및 삭제 실행
        if (!batchesToUpdate.isEmpty()) {
            stock_BatchesRepository.saveAll(batchesToUpdate);
            log.debug("{}개 배치 수량 업데이트 완료", batchesToUpdate.size());
        }

        if (!batchesToDelete.isEmpty()) {
            stock_BatchesRepository.deleteAll(batchesToDelete);
            log.debug("{}개 배치 삭제 완료", batchesToDelete.size());
        }

        // 차감 완료 후에도 남은 수량이 있다면 경고
        if (remainingToReduce > 0) {
            log.warn("⚠️ 배치 차감 완료했지만 {}개가 부족합니다 - 재고 ID: {}", remainingToReduce, inventoryId);
        }
    }

    /**
     * 모든 재고의 배치 일괄 조정 (배치 작업용)
     */
    public void adjustAllInventoryBatches() {
        log.info("전체 재고 배치 일괄 조정 시작");

        List<Inventory> allInventories = inventoryRepository.findAll();
        int successCount = 0;
        int errorCount = 0;

        for (Inventory inventory : allInventories) {
            try {
                adjustBatchesToTotalQuantity(inventory.getInventoryId());
                successCount++;

            } catch (Exception e) {
                errorCount++;
                log.error("재고 ID {} ('{}') 배치 조정 실패: {}",
                        inventory.getInventoryId(), inventory.getName(), e.getMessage(), e);
            }
        }

        log.info("전체 재고 배치 일괄 조정 완료 - 성공: {}개, 실패: {}개, 총: {}개",
                successCount, errorCount, allInventories.size());
    }

    /**
     * 특정 재고의 배치 일관성 검증
     * @return 총수량과 배치 합계가 일치하는지 여부
     */
    public boolean validateBatchConsistency(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("재고를 찾을 수 없습니다: ID " + inventoryId));

        Integer currentBatchTotal = stock_BatchesRepository.calculateTotalQuantityByInventoryId(inventoryId);
        currentBatchTotal = currentBatchTotal != null ? currentBatchTotal : 0;

        boolean isConsistent = inventory.getTotalQuantity().equals(currentBatchTotal);

        if (!isConsistent) {
            log.warn("⚠️ 배치 일관성 문제 발견 - 재고 ID: {}, 상품명: '{}', 총수량: {}, 배치 합계: {}",
                    inventoryId, inventory.getName(), inventory.getTotalQuantity(), currentBatchTotal);
        }

        return isConsistent;
    }

    /**
     * 모든 재고의 배치 일관성 검증
     * @return 일관성이 맞지 않는 재고 ID 목록
     */
    public List<Long> validateAllBatchConsistency() {
        log.info("전체 재고 배치 일관성 검증 시작");

        List<Inventory> allInventories = inventoryRepository.findAll();
        List<Long> inconsistentInventoryIds = new ArrayList<>();

        for (Inventory inventory : allInventories) {
            try {
                if (!validateBatchConsistency(inventory.getInventoryId())) {
                    inconsistentInventoryIds.add(inventory.getInventoryId());
                }
            } catch (Exception e) {
                log.error("재고 ID {} 배치 일관성 검증 실패: {}", inventory.getInventoryId(), e.getMessage());
                inconsistentInventoryIds.add(inventory.getInventoryId());
            }
        }

        log.info("전체 재고 배치 일관성 검증 완료 - 총 {}개 중 {}개 문제 발견",
                allInventories.size(), inconsistentInventoryIds.size());

        return inconsistentInventoryIds;
    }

    /**
     * 빈 배치 정리 (수량이 0인 배치들 삭제)
     */
    public void cleanupEmptyBatches() {
        log.info("빈 배치 정리 시작");

        stock_BatchesRepository.deleteEmptyBatches();

    }
}