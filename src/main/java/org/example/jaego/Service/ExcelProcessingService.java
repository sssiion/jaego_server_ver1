package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Dto.ExcelProcessResultDto;
import org.example.jaego.Dto.OrderExcelRowDto;
import org.example.jaego.Dto.StockExcelRowDto;
import org.example.jaego.Entity.Category;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.Stock_Batches;
import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExcelProcessingService {

    private final ExcelParsingService excelParsingService;
    private final InventoryRepository inventoryRepository;
    private final StockBatchRepository stock_BatchesRepository;
    private final CategoryRepository categoryRepository;
    private final StockBatchAutoService stock_BatchesAutoService;

    // 재고(출고) 엑셀 처리
    public ExcelProcessResultDto processStockExcel(MultipartFile file) {
        log.info("재고 엑셀 처리 시작");
        long startTime = System.currentTimeMillis();

        // 1. 엑셀 파싱
        List<StockExcelRowDto> stockRows = excelParsingService.parseStockExcel(file);

        // 2. 유효한 데이터만 필터링
        List<StockExcelRowDto> validRows = stockRows.stream()
                .filter(StockExcelRowDto::isValid)
                .collect(Collectors.toList());

        List<String> errorMessages = stockRows.stream()
                .filter(row -> !row.isValid())
                .map(StockExcelRowDto::getErrorMessage)
                .collect(Collectors.toList());

        // 3. 상품 존재 여부 및 재고 충분성 검증
        Map<String, StockExcelRowDto> validRowMap = validRows.stream()
                .collect(Collectors.toMap(StockExcelRowDto::getProductName, row -> row));

        List<String> productNames = new ArrayList<>(validRowMap.keySet());
        List<Inventory> existingInventories = inventoryRepository.findByNameInOrderByName(productNames);

        Map<String, Inventory> inventoryMap = existingInventories.stream()
                .collect(Collectors.toMap(Inventory::getName, inv -> inv));

        // 4. 재고 부족 검증
        List<StockExcelRowDto> processableRows = new ArrayList<>();

        for (StockExcelRowDto row : validRows) {
            Inventory inventory = inventoryMap.get(row.getProductName());

            if (inventory == null) {
                errorMessages.add(String.format("상품 '%s'을(를) 찾을 수 없습니다.", row.getProductName()));
                continue;
            }

            if (inventory.getTotalQuantity() < row.getSalesQuantity()) {
                errorMessages.add(String.format("상품 '%s' 재고 부족: 현재 %d개, 요청 %d개",
                        row.getProductName(), inventory.getTotalQuantity(), row.getSalesQuantity()));
                continue;
            }

            processableRows.add(row);
        }

        // 5. 실제 재고 차감 처리 (FIFO)
        List<String> processedProducts = new ArrayList<>();

        for (StockExcelRowDto row : processableRows) {
            try {
                Inventory inventory = inventoryMap.get(row.getProductName());

                // FIFO 재고 차감
                reduceStockFIFO(inventory.getInventoryId(), row.getSalesQuantity());

                // 총수량을 남은재고수량으로 업데이트
                inventory.setTotalQuantity(row.getRemainingStock());
                inventoryRepository.save(inventory);

                // 배치 자동 조정
                stock_BatchesAutoService.adjustBatchesToTotalQuantity(inventory.getInventoryId());

                processedProducts.add(row.getProductName());
                log.info("재고 차감 완료: {} - {}개 차감, 남은 수량: {}개",
                        row.getProductName(), row.getSalesQuantity(), row.getRemainingStock());

            } catch (Exception e) {
                errorMessages.add(String.format("상품 '%s' 처리 실패: %s", row.getProductName(), e.getMessage()));
                log.error("재고 차감 실패: {}", row.getProductName(), e);
            }
        }

        long endTime = System.currentTimeMillis();

        return ExcelProcessResultDto.builder()
                .processingType("재고")
                .totalRows(stockRows.size())
                .processedRows(processedProducts.size())
                .errorRows(stockRows.size() - processedProducts.size())
                .errorMessages(errorMessages)
                .processedProducts(processedProducts)
                .processingTime(String.format("%.2f초", (endTime - startTime) / 1000.0))
                .build();
    }

    // 발주(입고) 엑셀 처리
    public ExcelProcessResultDto processOrderExcel(MultipartFile file) {
        log.info("발주 엑셀 처리 시작");
        long startTime = System.currentTimeMillis();

        // 1. 엑셀 파싱
        List<OrderExcelRowDto> orderRows = excelParsingService.parseOrderExcel(file);

        // 2. 유효한 데이터만 필터링
        List<OrderExcelRowDto> validRows = orderRows.stream()
                .filter(OrderExcelRowDto::isValid)
                .collect(Collectors.toList());

        List<String> errorMessages = orderRows.stream()
                .filter(row -> !row.isValid())
                .map(OrderExcelRowDto::getErrorMessage)
                .collect(Collectors.toList());

        // 3. 상품 조회 및 생성
        List<String> productNames = validRows.stream()
                .map(OrderExcelRowDto::getProductName)
                .distinct()
                .collect(Collectors.toList());

        List<Inventory> existingInventories = inventoryRepository.findByNameInOrderByName(productNames);
        Map<String, Inventory> inventoryMap = existingInventories.stream()
                .collect(Collectors.toMap(Inventory::getName, inv -> inv));

        // 4. 카테고리 조회 및 생성
        Set<String> categoryNames = validRows.stream()
                .map(OrderExcelRowDto::getCategoryName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Category> existingCategories = categoryRepository.findAll();
        Map<String, Category> categoryMap = existingCategories.stream()
                .collect(Collectors.toMap(Category::getCategory, cat -> cat));

        // 5. 실제 발주 처리
        List<String> processedProducts = new ArrayList<>();

        for (OrderExcelRowDto row : validRows) {
            try {
                // 카테고리 처리
                Category category = null;
                if (row.getCategoryName() != null) {
                    category = categoryMap.get(row.getCategoryName());
                    if (category == null) {
                        // 카테고리 자동 생성
                        category = Category.builder()
                                .category(row.getCategoryName())
                                .categoryType("발주")
                                .build();
                        category = categoryRepository.save(category);
                        categoryMap.put(row.getCategoryName(), category);

                        log.info("새 카테고리 생성: {}", row.getCategoryName());
                    }
                }

                // 상품 처리
                Inventory inventory = inventoryMap.get(row.getProductName());
                if (inventory == null) {
                    // 상품 자동 생성
                    inventory = Inventory.builder()
                            .name(row.getProductName())
                            .category(category)
                            .totalQuantity(row.getOrderQuantity())
                            .build();
                    inventory = inventoryRepository.save(inventory);
                    inventoryMap.put(row.getProductName(), inventory);

                    log.info("새 상품 생성: {} (카테고리: {})", row.getProductName(),
                            category != null ? category.getCategory() : "미분류");
                } else {
                    // 기존 상품에 수량 추가
                    inventory.setTotalQuantity(inventory.getTotalQuantity() + row.getOrderQuantity());

                    // 카테고리 업데이트 (기존에 없었다면)
                    if (inventory.getCategory() == null && category != null) {
                        inventory.setCategory(category);
                        log.info("상품 '{}' 카테고리 설정: {}", row.getProductName(), category.getCategory());
                    }

                    inventoryRepository.save(inventory);
                }

                // 배치 추가 (유통기한 있는 경우와 없는 경우)
                addStock_Batches(inventory.getInventoryId(), row.getOrderQuantity(), row.getExpiryDate());

                // 배치 자동 조정
                stock_BatchesAutoService.adjustBatchesToTotalQuantity(inventory.getInventoryId());

                processedProducts.add(row.getProductName());
                log.info("발주 처리 완료: {} - {}개 추가 (유통기한: {})",
                        row.getProductName(), row.getOrderQuantity(),
                        row.getExpiryDate() != null ? row.getExpiryDate() : "없음");

            } catch (Exception e) {
                errorMessages.add(String.format("상품 '%s' 처리 실패: %s", row.getProductName(), e.getMessage()));
                log.error("발주 처리 실패: {}", row.getProductName(), e);
            }
        }

        long endTime = System.currentTimeMillis();

        return ExcelProcessResultDto.builder()
                .processingType("발주")
                .totalRows(orderRows.size())
                .processedRows(processedProducts.size())
                .errorRows(orderRows.size() - processedProducts.size())
                .errorMessages(errorMessages)
                .processedProducts(processedProducts)
                .processingTime(String.format("%.2f초", (endTime - startTime) / 1000.0))
                .build();
    }

    // FIFO 재고 차감 (Node.js 로직과 동일)
    private void reduceStockFIFO(Long inventoryId, Integer reduceQuantity) {
        List<Stock_Batches> batches = stock_BatchesRepository.findForFIFOProcessing(inventoryId);

        int remainingToReduce = reduceQuantity;
        List<Stock_Batches> batchesToUpdate = new ArrayList<>();
        List<Stock_Batches> batchesToDelete = new ArrayList<>();

        for (Stock_Batches batch : batches) {
            if (remainingToReduce <= 0) break;

            if (batch.getQuantity() <= remainingToReduce) {
                // 배치 전체 소진
                remainingToReduce -= batch.getQuantity();
                batchesToDelete.add(batch);
            } else {
                // 배치 일부 차감
                batch.setQuantity(batch.getQuantity() - remainingToReduce);
                batchesToUpdate.add(batch);
                remainingToReduce = 0;
            }
        }

        // 배치 업데이트 및 삭제
        if (!batchesToUpdate.isEmpty()) {
            stock_BatchesRepository.saveAll(batchesToUpdate);
        }
        if (!batchesToDelete.isEmpty()) {
            stock_BatchesRepository.deleteAll(batchesToDelete);
        }

        if (remainingToReduce > 0) {
            throw new IllegalStateException(String.format("재고 부족: %d개 부족", remainingToReduce));
        }
    }

    // 배치 추가
    private void addStock_Batches(Long inventoryId, Integer quantity, LocalDate expiryDate) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다"));

        if (expiryDate == null) {
            // null 유통기한 배치에 추가
            List<Stock_Batches> nullBatches = stock_BatchesRepository
                    .findNullExpiryBatchesByInventoryId(inventoryId);

            if (!nullBatches.isEmpty()) {
                // 기존 null 배치에 수량 추가
                Stock_Batches existingBatch = nullBatches.get(0);
                existingBatch.setQuantity(existingBatch.getQuantity() + quantity);
                stock_BatchesRepository.save(existingBatch);
            } else {
                // 새 null 배치 생성
                Stock_Batches newBatch = Stock_Batches.builder()
                        .inventory(inventory)
                        .quantity(quantity)
                        .expiryDate(null)
                        .build();
                stock_BatchesRepository.save(newBatch);
            }
        } else {
            // 유통기한 있는 새 배치 생성
            Stock_Batches newBatch = Stock_Batches.builder()
                    .inventory(inventory)
                    .quantity(quantity)
                    .expiryDate(expiryDate)
                    .build();
            stock_BatchesRepository.save(newBatch);
        }
    }
}