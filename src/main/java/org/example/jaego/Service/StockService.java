package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.Stock_Batches;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository stockBatchRepository;

    // FIFO 기반 재고 차감 (Node.js 로직과 동일)
    public void reduceStockFIFO(Long inventoryId, Integer reduceQuantity) {
        List<Stock_Batches> batches = stockBatchRepository.findByInventoryIdForFIFO(inventoryId);

        int remainingToReduce = reduceQuantity;

        for (Stock_Batches batch : batches) {
            if (remainingToReduce <= 0) break;

            if (batch.getQuantity() <= remainingToReduce) {
                // 배치 전체 소진
                remainingToReduce -= batch.getQuantity();
                stockBatchRepository.delete(batch);
            } else {
                // 배치 일부 차감
                batch.setQuantity(batch.getQuantity() - remainingToReduce);
                stockBatchRepository.save(batch);
                remainingToReduce = 0;
            }
        }

        // 총 수량 업데이트
        updateInventoryTotalQuantity(inventoryId);
    }

    // 재고 추가 (유통기한 null 우선)
    public void addStock(Long inventoryId, Integer addQuantity, LocalDate expiryDate) {
        if (expiryDate == null) {
            // null 유통기한 배치 찾기
            List<Stock_Batches> nullExpiryBatches = stockBatchRepository
                    .findByInventoryIdForFIFO(inventoryId)
                    .stream()
                    .filter(batch -> batch.getExpiryDate() == null)
                    .collect(Collectors.toList());

            if (!nullExpiryBatches.isEmpty()) {
                // 기존 null 배치에 수량 추가
                Stock_Batches nullBatch = nullExpiryBatches.get(0);
                nullBatch.setQuantity(nullBatch.getQuantity() + addQuantity);
                stockBatchRepository.save(nullBatch);
            } else {
                // 새 null 배치 생성
                createNewBatch(inventoryId, addQuantity, null);
            }
        } else {
            // 유통기한 있는 배치 생성
            createNewBatch(inventoryId, addQuantity, expiryDate);
        }

        // 총 수량 업데이트
        updateInventoryTotalQuantity(inventoryId);
    }

    private void createNewBatch(Long inventoryId, Integer quantity, LocalDate expiryDate) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다"));

        Stock_Batches batch = Stock_Batches.builder()
                .inventory(inventory)
                .quantity(quantity)
                .expiryDate(expiryDate)
                .build();

        stockBatchRepository.save(batch);
    }

    // 총 수량 업데이트 (Node.js의 updateInventoryTotalQuantity와 동일)
    public void updateInventoryTotalQuantity(Long inventoryId) {
        Integer totalQuantity = stockBatchRepository.calculateTotalQuantityByInventoryId(inventoryId);
        inventoryRepository.updateTotalQuantity(inventoryId, totalQuantity != null ? totalQuantity : 0);
    }
}
