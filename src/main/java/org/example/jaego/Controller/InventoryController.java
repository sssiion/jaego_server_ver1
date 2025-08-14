package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;

import org.example.jaego.Service.InventoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

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
}
