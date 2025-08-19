package org.example.jaego.Service;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Dto.ExcelProcessResultDto;
import org.example.jaego.Dto.OrderExcelRowDto;
import org.example.jaego.Dto.StockExcelRowDto;
import org.example.jaego.Entity.Category;
import org.example.jaego.Entity.Inventory;

import org.example.jaego.Entity.stockBatches;
import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.InventoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExcelProcessingService {

    private final ExcelParsingService parsing;
    private final InventoryRepository invRepo;
    private final StockBatchRepository batchRepo;
    private final CategoryRepository catRepo;
    private final StockBatchAutoService batchAutoService;

    public ExcelProcessResultDto processStockExcel(MultipartFile file) {
        long start = System.currentTimeMillis();
        List<StockExcelRowDto> rows = parsing.parseStockExcel(file);
        List<String> errors = new ArrayList<>();
        List<String> processed = new ArrayList<>();

        Map<String, Inventory> invMap = invRepo
                .findByNameInOrderByName(rows.stream().map(StockExcelRowDto::getProductName).toList())
                .stream().collect(Collectors.toMap(Inventory::getName, i -> i));
        // 카테고리 정보도 미리 담고오면 필요시 신규 Inventory에 세팅 가능

        for (StockExcelRowDto r : rows) {
            Inventory inv = invMap.get(r.getProductName());
            if (inv == null) {
                //errors.add("상품 없음: "+r.getProductName());

                inv = invRepo.save(Inventory.builder()
                        .name(r.getProductName())
                        .category(null) // 카테고리가 필요 없으면 null로 둘 수 있음
                        .totalQuantity(r.getRemainingStock())  // 재고 처음 생성시 0으로 초기화
                        .build());
                invMap.put(r.getProductName(), inv);
            }
            if (inv.getTotalQuantity() < r.getSalesQuantity()) {
                //errors.add("재고 부족: "+r.getProductName()); continue;
            }
            reduceFIFO(inv.getInventoryId(), r.getSalesQuantity());
            inv.setTotalQuantity(r.getRemainingStock());
            invRepo.save(inv);
            batchAutoService.adjustBatchesToTotalQuantity(inv.getInventoryId());
            processed.add(r.getProductName());
        }

        return ExcelProcessResultDto.builder()
                .processingType("재고")
                .totalRows(rows.size())
                .processedRows(processed.size())
                .errorRows(errors.size())
                .errorMessages(errors)
                .processedProducts(processed)
                .processingTime((System.currentTimeMillis()-start)/1000.0+"초")
                .build();
    }

    public ExcelProcessResultDto processOrderExcel(MultipartFile file) {
        long start = System.currentTimeMillis();
        List<OrderExcelRowDto> rows = parsing.parseOrderExcel(file);
        List<String> errors = new ArrayList<>();
        List<String> processed = new ArrayList<>();

        Map<String, Inventory> invMap = invRepo
                .findByNameInOrderByName(rows.stream().map(OrderExcelRowDto::getProductName).toList())
                .stream().collect(Collectors.toMap(Inventory::getName, i -> i));

        Map<String, Category> catMap = catRepo.findAll()
                .stream().collect(Collectors.toMap(Category::getCategory, c -> c));

        for (OrderExcelRowDto r : rows) {
            try {
                Category cat = null;
                if (r.getCategoryName()!=null) {
                    cat = catMap.computeIfAbsent(r.getCategoryName(),
                            cName -> catRepo.save(Category.builder().category(cName).build()));
                }
                Inventory inv = invMap.get(r.getProductName());
                if (inv==null) {
                    inv = invRepo.save(Inventory.builder().name(r.getProductName())
                            .category(cat).totalQuantity(r.getOrderQuantity()).build());
                    invMap.put(r.getProductName(), inv);
                } else {
                    inv.setTotalQuantity(inv.getTotalQuantity() + r.getOrderQuantity());
                    if (inv.getCategory()==null && cat!=null) inv.setCategory(cat);
                    invRepo.save(inv);
                }
                addBatch(inv.getInventoryId(), r.getOrderQuantity(), r.getExpiryDate());
                batchAutoService.adjustBatchesToTotalQuantity(inv.getInventoryId());
                processed.add(r.getProductName());
            } catch (Exception e) {
                errors.add("상품 처리 실패: "+r.getProductName()+" - "+e.getMessage());
            }
        }

        return ExcelProcessResultDto.builder()
                .processingType("발주")
                .totalRows(rows.size())
                .processedRows(processed.size())
                .errorRows(errors.size())
                .errorMessages(errors)
                .processedProducts(processed)
                .processingTime((System.currentTimeMillis()-start)/1000.0+"초")
                .build();
    }

    private void reduceFIFO(Long invId, int qty) {
        List<stockBatches> batches = batchRepo.findForFIFOProcessing(invId);
        int remain = qty;
        for (stockBatches b : batches) {
            if (remain<=0) break;
            if (b.getQuantity() <= remain) {
                remain -= b.getQuantity();
                batchRepo.delete(b);
            } else {
                b.setQuantity(b.getQuantity()-remain);
                batchRepo.save(b);
                remain=0;
            }
        }
        if (remain>0) throw new IllegalStateException("재고 부족 "+remain);
    }

    private void addBatch(Long invId, int qty, java.time.LocalDateTime expiry) {
        Inventory inv = invRepo.findById(invId).orElseThrow();
        if (expiry == null) {
            List<stockBatches> nullBatches = batchRepo.findNullExpiryBatchesByInventoryId(invId);
            if (!nullBatches.isEmpty()) {
                stockBatches b = nullBatches.get(0);
                b.setQuantity(b.getQuantity()+qty);
                batchRepo.save(b);
                return;
            }
        }
        batchRepo.save(stockBatches.builder().inventory(inv).quantity(qty).expiryDate(expiry).build());
    }
}
