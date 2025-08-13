package org.example.jaego.Dto;

import lombok.*;

import java.time.LocalDate;

// 발주(입고) 엑셀 데이터
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderExcelRowDto {
    private String categoryName;     // 카테고리
    private String productName;      // 상품명
    private Integer orderQuantity;   // 발주수량
    private LocalDate expiryDate;    // 유통기한 (선택사항)

    // 검증용 필드
    private boolean isValid = true;
    private String errorMessage;
}