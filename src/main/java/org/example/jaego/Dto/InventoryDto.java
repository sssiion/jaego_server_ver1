package org.example.jaego.Dto;

import lombok.*;
import org.example.jaego.Entity.Inventory;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long inventoryId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Integer totalQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public InventoryDto(Inventory inventory) {
        this.inventoryId = inventory.getInventoryId();
        this.name = inventory.getName();
        this.categoryId = inventory.getCategory().getCategoryId();
        this.totalQuantity = inventory.getTotalQuantity();
        this.createdAt = inventory.getCreatedAt();
        this.updatedAt = inventory.getUpdatedAt();
    }
}
