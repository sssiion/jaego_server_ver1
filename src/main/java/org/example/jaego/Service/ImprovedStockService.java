package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import org.example.jaego.Entity.Stock_Batches;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ImprovedStockService {

    private  final StockBatchRepository stockBatchRepository;

    // 개선된 재고 차감 (null 유통기한 우선 소진)
    public void reduceStockWithNullPriority(Long inventoryId, Integer reduceQuantity) {
        // 1단계: null 유통기한 배치부터 소진
        List<Stock_Batches> nullBatches = stockBatchRepository.findNullExpiryBatchesByInventoryId(inventoryId);
        int remainingToReduce = reduceQuantity;

        for (Stock_Batches batch : nullBatches) {
            if (remainingToReduce <= 0) break;

            if (batch.getQuantity() <= remainingToReduce) {
                remainingToReduce -= batch.getQuantity();
                stockBatchRepository.delete(batch);
            } else {
                batch.setQuantity(batch.getQuantity() - remainingToReduce);
                stockBatchRepository.save(batch);
                remainingToReduce = 0;
            }
        }

        // 2단계: null 배치로 부족하면 유통기한 있는 배치에서 FIFO 소진
        if (remainingToReduce > 0) {
            List<Stock_Batches> expiryBatches = stockBatchRepository.findExpiryBatchesByInventoryId(inventoryId);

            for (Stock_Batches batch : expiryBatches) {
                if (remainingToReduce <= 0) break;

                if (batch.getQuantity() <= remainingToReduce) {
                    remainingToReduce -= batch.getQuantity();
                    stockBatchRepository.delete(batch);
                } else {
                    batch.setQuantity(batch.getQuantity() - remainingToReduce);
                    stockBatchRepository.save(batch);
                    remainingToReduce = 0;
                }
            }
        }

        updateInventoryTotalQuantity(inventoryId);
    }

    // null 유통기한 재고 우선 추가
    public void addNullExpiryStock(Long inventoryId, Integer addQuantity) {
        List<Stock_Batches> nullBatches = stockBatchRepository.findNullExpiryBatchesByInventoryId(inventoryId);

        if (!nullBatches.isEmpty()) {
            // 기존 null 배치에 추가
            Stock_Batches existingBatch = nullBatches.get(0);
            existingBatch.setQuantity(existingBatch.getQuantity() + addQuantity);
            stockBatchRepository.save(existingBatch);
        } else {
            // 새 null 배치 생성
            createNewBatch(inventoryId, addQuantity, null);
        }

        updateInventoryTotalQuantity(inventoryId);
    }
}
