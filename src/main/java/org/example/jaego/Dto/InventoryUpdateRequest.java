package org.example.jaego.Dto;


import lombok.Data;

@Data
public class InventoryUpdateRequest {
    private String name;
    private Long categoryId;
}
