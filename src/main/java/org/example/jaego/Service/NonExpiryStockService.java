package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import org.example.jaego.Entity.Stock_Batches;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NonExpiryStockService {

    private final StockBatchRepository stockBatchRepository;

    // 유통기한 없는 재고만 조회
    public List<Stock_Batches> getAllNonExpiryStocks() {
        List<Stock_Batches> batches = stockBatchRepository.findAllNullExpiryBatches();
        return batches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 특정 상품의 유통기한 없는 배치들
    public List<Stock_Batches> getNonExpiryStocksByProduct(Long inventoryId) {
        List<Stock_Batches> batches = stockBatchRepository.findNullExpiryBatchesByInventoryId(inventoryId);
        return batches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 카테고리별 유통기한 없는 재고
    public List<StockBatchDto> getNonExpiryStocksByCategory(String categoryName) {
        List<Stock_Batches> batches = stockBatchRepository.findNullExpiryBatchesByCategory(categoryName);
        return batches.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
