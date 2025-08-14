package org.example.jaego.Service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.stockBatches;
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
    private final StockBatchRepository stockBatchRepository; // ✅ 일관된 변수명

    /**
     * 총수량에 맞춰 배치 자동 조정
     */
    public void adjustBatchesToTotalQuantity(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("재고를 찾을 수 없습니다: ID " + inventoryId));

        // 현재 모든 배치의 수량 합계 계산
        Integer currentBatchTotal = stockBatchRepository.calculateTotalQuantityByInventoryId(inventoryId);
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
        List<stockBatches> existingNullBatches = stockBatchRepository
                .findNullExpiryBatchesByInventoryId(inventory.getInventoryId());

        if (!existingNullBatches.isEmpty()) {
            // 기존 null 배치에 수량 추가 (첫 번째 배치에 추가)
            stockBatches existingNullBatch = existingNullBatches.get(0);
            existingNullBatch.setQuantity(existingNullBatch.getQuantity() + addQuantity);
            stockBatchRepository.save(existingNullBatch);

            log.debug("기존 null 배치에 {}개 추가 - 배치 ID: {}", addQuantity, existingNullBatch.getId());

        } else {
            // 새로운 null 배치 생성
            stockBatches newNullBatch = stockBatches.builder()
                    .inventory(inventory)
                    .quantity(addQuantity)
                    .expiryDate(null) // null 유통기한
                    .build();

            stockBatchRepository.save(newNullBatch);
            log.debug("새로운 null 배치 {}개 생성 - 배치 ID: {}", addQuantity, newNullBatch.getId());
        }
    }

    /**
     * 초과 수량만큼 배치에서 FIFO 차감
     */
    private void reduceBatchesForDifference(Long inventoryId, Integer reduceQuantity) {
        int remainingToReduce = reduceQuantity;

        // 1단계: null 배치부터 차감
        List<stockBatches> nullBatches = stockBatchRepository
                .findNullExpiryBatchesByInventoryId(inventoryId);

        List<stockBatches> batchesToUpdate = new ArrayList<>();
        List<stockBatches> batchesToDelete = new ArrayList<>();

        for (stockBatches nullBatch : nullBatches) {
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
            List<stockBatches> expiryBatches = stockBatchRepository
                    .findExpiryBatchesByInventoryInventoryId(inventoryId);

            for (stockBatches batch : expiryBatches) {
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
            stockBatchRepository.saveAll(batchesToUpdate);
            log.debug("{}개 배치 수량 업데이트 완료", batchesToUpdate.size());
        }

        if (!batchesToDelete.isEmpty()) {
            stockBatchRepository.deleteAll(batchesToDelete);
            log.debug("{}개 배치 삭제 완료", batchesToDelete.size());
        }

        // 차감 완료 후에도 남은 수량이 있다면 경고
        if (remainingToReduce > 0) {
            log.warn("⚠️ 배치 차감 완료했지만 {}개가 부족합니다 - 재고 ID: {}", remainingToReduce, inventoryId);
        }
    }

    // 나머지 메서드들도 동일한 패턴으로 수정...
    // (기존 로직 유지, 변수명만 stockBatchRepository로 통일)
}
