package org.example.jaego.Dto;

import lombok.Data;

@Data
public class InventoryCreateRequest {
    private String name;
    private Long categoryId;
}
