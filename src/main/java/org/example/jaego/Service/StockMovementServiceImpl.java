package org.example.jaego.Service;

import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.StockMovement;

import org.example.jaego.Exception.InventoryNotFoundException;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockMovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public OperationResult recordStockUsage(StockMovementDto movementDto) {
        Inventory inventory = inventoryRepository.findById(movementDto.getInventoryId())
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다."));

        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .movementType(movementDto.getMovementType())
                .quantity(movementDto.getQuantity())
                .reason(movementDto.getReason())
                .createdBy(movementDto.getCreatedBy())
                .build();

        stockMovementRepository.save(movement);
        return OperationResult.success("이동 이력이 기록되었습니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementDto> getStockHistory(Long inventoryId) {
        return stockMovementRepository.findByInventoryInventoryIdOrderByCreatedAtDesc(inventoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsageStatisticsDto> getUsageStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return stockMovementRepository.getUsageStatisticsByPeriod(startDate, endDate).stream()
                .map(m -> UsageStatisticsDto.builder()
                        .inventoryId(m.getInventory().getInventoryId())
                        .inventoryName(m.getInventory().getName())
                        .movementType(m.getMovementType())
                        .quantity(m.getQuantity())
                        .date(m.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }

    private StockMovementDto convertToDto(StockMovement movement) {
        return StockMovementDto.builder()
                .movementId(movement.getMovementId())
                .inventoryId(movement.getInventory().getInventoryId())
                .movementType(movement.getMovementType())
                .quantity(movement.getQuantity())
                .reason(movement.getReason())
                .createdAt(movement.getCreatedAt())
                .createdBy(movement.getCreatedBy())
                .build();
    }
}
