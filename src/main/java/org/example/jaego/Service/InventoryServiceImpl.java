package org.example.jaego.Service;



import org.example.jaego.Entity.Category;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Exception.CategoryNotFoundException;
import org.example.jaego.Exception.InventoryNotFoundException;
import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {


    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockBatchRepository stockBatchesRepository;

    @Override
    public InventoryDto setCategory(Long inventoryId, Long categoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElse(null);
        if(inventory == null) {
            return null;
        }
        inventory.setCategory(categoryRepository.findById(categoryId).orElse(null));
        inventoryRepository.save(inventory);

        return  new InventoryDto(inventory);
    }
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getInventoriesByCategory(Long categoryId, String searchKeyword) {
        List<Inventory> inventories;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            inventories = inventoryRepository.findByCategoryCategoryIdAndNameContainingIgnoreCaseOrderByName(
                    categoryId, searchKeyword.trim());
        } else {
            inventories = inventoryRepository.findByCategoryIdOrderByEarliestExpiryDate(categoryId);
        }

        return inventories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<InventoryDto> getInventoryByCategory(Long categoryId){
         List<Inventory> inventories = inventoryRepository.findByCategory_CategoryId(categoryId);
         return inventories.stream().map(inventory -> convertToDto(inventory)).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventorySearchDto> searchInventories(String keyword) {
        List<Inventory> inventories = inventoryRepository
                .findByNameContainingIgnoreCaseOrderByEarliestExpiryDate(keyword);

        return inventories.stream()
                .map(inventory -> {
                    LocalDateTime earliestExpiry = stockBatchesRepository
                            .getEarliestExpiryDateByInventoryId(inventory.getInventoryId());

                    return InventorySearchDto.builder()
                            .inventoryId(inventory.getInventoryId())
                            .name(inventory.getName())
                            .categoryName(inventory.getCategory() != null ?
                                    inventory.getCategory().getCategory() : "미분류")
                            .totalQuantity(inventory.getTotalQuantity())
                            .earliestExpiryDate(earliestExpiry)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDetailDto getInventoryDetails(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        // 배치 정보 조회
        var batches = stockBatchesRepository.findByInventoryIdOrderByExpiryDateNullsFirst(inventoryId);
        var batchDtos = batches.stream()
                .map(batch -> StockBatchDto.builder()
                        .id(batch.getId())
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .createdAt(batch.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return InventoryDetailDto.builder()
                .inventoryId(inventory.getInventoryId())
                .name(inventory.getName())
                .categoryId(inventory.getCategory() != null ? inventory.getCategory().getCategoryId() : null)
                .categoryName(inventory.getCategory() != null ? inventory.getCategory().getCategory() : "미분류")
                .totalQuantity(inventory.getTotalQuantity())
                .stockBatches(batchDtos)
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    @Override
    public InventoryDto createInventory(InventoryCreateRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));
        }

        Inventory inventory = Inventory.builder()
                .name(request.getName())
                .category(category)
                .totalQuantity(0) // 초기 수량은 0
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        return convertToDto(savedInventory);
    }

    @Override
    public InventoryDto updateInventory(Long inventoryId, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + request.getCategoryId()));
        }

        inventory.setName(request.getName());
        inventory.setCategory(category);

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return convertToDto(updatedInventory);
    }

    @Override
    public void deleteInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        inventoryRepository.delete(inventory);
    }

    @Override
    public void updateTotalQuantity(Long inventoryId) {
        Integer totalQuantity = stockBatchesRepository.getTotalQuantityByInventoryId(inventoryId);

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        inventory.setTotalQuantity(totalQuantity != null ? totalQuantity : 0);
        inventoryRepository.save(inventory);
    }
    @Override
    public List<StockBatchDto> findBatchesExpiringWithin(int minutes) {
        // 1. 조회 범위 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureLimit = now.plusMinutes(minutes);

        // 2. Repository 메소드 호출
        // "수량이 0보다 크고, 유통기한이 지금(now)과 30분 후(futureLimit) 사이인 모든 배치"를 조회
        return stockBatchesRepository.findByQuantityGreaterThanAndExpiryDateBetween(0, now, futureLimit)
                .stream()
                .map(batch -> StockBatchDto.builder() // 3. 조회 결과를 DTO로 변환
                        .id(batch.getId())
                        .inventoryName(batch.getInventory().getName())
                        .quantity(batch.getQuantity())
                        .expiryDate(batch.getExpiryDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySummaryDto getInventorySummary(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        LocalDateTime earliestExpiry = stockBatchesRepository.getEarliestExpiryDateByInventoryId(inventoryId);
        Long urgentBatchCount = stockBatchesRepository.countUrgentBatchesByDays(LocalDateTime.now().plusDays(7));

        return InventorySummaryDto.builder()
                .inventoryId(inventory.getInventoryId())
                .name(inventory.getName())
                .categoryName(inventory.getCategory() != null ? inventory.getCategory().getCategory() : "미분류")
                .totalQuantity(inventory.getTotalQuantity())
                .earliestExpiryDate(earliestExpiry)
                .urgentBatchCount(urgentBatchCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UrgentInventoryDto> findInventoriesWithUrgentBatches(Integer days) {
        List<Inventory> inventories = inventoryRepository.findInventoriesWithUrgentBatches(days);

        return inventories.stream()
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
    // 최근 업데이트 된 거 조회
    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> getRecentlyUpdatedInventories() {
        List<Inventory> inventories = inventoryRepository.findTop10ByOrderByUpdatedAtDesc();

        return inventories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventoryById(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new InventoryNotFoundException("재고를 찾을 수 없습니다: " + inventoryId));

        return convertToDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDto> getAllInventories(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);

        return inventories.map(this::convertToDto);
    }

    private InventoryDto convertToDto(Inventory inventory) {
        return InventoryDto.builder()
                .inventoryId(inventory.getInventoryId())
                .name(inventory.getName())
                .categoryId(inventory.getCategory() != null ? inventory.getCategory().getCategoryId() : null)
                .categoryName(inventory.getCategory() != null ? inventory.getCategory().getCategory() : "미분류")
                .totalQuantity(inventory.getTotalQuantity())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
