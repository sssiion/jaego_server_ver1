package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;

import org.example.jaego.Entity.Category;
import org.example.jaego.Service.CategoryService;
import org.example.jaego.Service.InventoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final CategoryService categoryService;


    @GetMapping("/userId/{ID}")
    public void setuserId(@PathVariable String ID){
       inventoryService.setUserId(ID);
    }
    //카테고리 지정하기
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<InventoryDto> setCategory(@RequestParam(required = false) Long inventoryId, @PathVariable Long categoryId) {
        return ResponseEntity.ok(inventoryService.setCategory(inventoryId, categoryId));
    }

    // 카테고리 아이디로 가져오기
    @GetMapping("/category")
    public ResponseEntity<List<InventoryDto>> getAllInventory(@RequestParam(required = false) Long categoryId){
        return ResponseEntity.ok(inventoryService.getInventoryByCategory(categoryId));
    }

    @GetMapping
    public List<InventoryDto> getAllInventories(Pageable pageable) {
        return inventoryService.getAllInventories(pageable).getContent();
    }

    @GetMapping("/{id}")
    public InventoryDetailDto getInventoryDetails(@PathVariable Long id) {
        return inventoryService.getInventoryDetails(id);
    }

    @PostMapping
    public ResponseEntity<InventoryDto> createInventory(@RequestBody InventoryCreateRequest request) {
        return ResponseEntity.ok(inventoryService.createInventory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> updateInventory(@PathVariable Long id, @RequestBody InventoryUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<InventorySearchDto> searchInventories(@RequestParam String keyword) {
        return inventoryService.searchInventories(keyword);
    }

    @GetMapping("/urgent")
    public List<UrgentInventoryDto> getUrgentInventories(@RequestParam(defaultValue = "7") Integer days) {
        return inventoryService.findInventoriesWithUrgentBatches(days);
    }
    // 만료 임박 제품
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<StockBatchDto>> getExpiringSoonBatches(
            @RequestParam(defaultValue = "30") int minutes) {

        List<StockBatchDto> expiringBatches = inventoryService.findBatchesExpiringWithin(minutes);
        return ResponseEntity.ok(expiringBatches);
    }
}
