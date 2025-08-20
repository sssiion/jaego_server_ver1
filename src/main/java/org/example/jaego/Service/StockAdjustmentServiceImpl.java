package org.example.jaego.Service;


import org.example.jaego.Entity.Inventory;

import org.example.jaego.Exception.InsufficientStockException;
import org.example.jaego.Exception.InventoryNotFoundException;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockBatchRepository stockBatchesRepository;

    @Autowired
    private StockBatchService stockBatchService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public OperationResult increaseStock(StockAdjustmentRequest request) {
        stockBatchService.addStock(
                StockAdditionRequest.builder()
                        .inventoryId(request.getInventoryId())
                        .quantity(request.getQuantity())
                        .expiryDate(request.getExpiryDate())
                        .build()
        );
        return OperationResult.success("재고가 추가되었습니다.");
    }

    @Override
    public OperationResult decreaseStock(StockAdjustmentRequest request) {
        return stockBatchService.reduceStock(
                StockReductionRequest.builder()
                        .inventoryId(request.getInventoryId())
                        .quantity(request.getQuantity())
                        .build()
        );
    }

    @Override
    public OperationResult adjustStockQuantity(StockAdjustmentRequest request) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다."));

        //if (request.getQuantity() < 0) {
         //   throw new InsufficientStockException("수량은 0 이상이어야 합니다.");
        //}

        inventory.setTotalQuantity(request.getQuantity());
        inventoryRepository.save(inventory);
        return OperationResult.success("수량이 수정되었습니다.");
    }



    @Override
    public OperationResult bulkStockAdjustment(BulkAdjustmentRequest request) {
        List<String> results = new ArrayList<>();
        for (StockAdjustmentRequest adj : request.getAdjustments()) {
            try {
                adjustStockQuantity(adj);
                //results.add("재고 ID " + adj.getInventoryId() + " 수정 성공");
            } catch (Exception e) {
                results.add("재고 ID " + adj.getInventoryId() + " 수정 실패: " + e.getMessage());
            }
        }
        return OperationResult.builder()
                .success(true)
                .details(results)//.message("일괄 재고 조정 완료");
                .build();

    }
}
