package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryWithInventoryCountDto {
    private Long categoryId;
    private String category;
    private String categoryType;
    private Long inventoryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
