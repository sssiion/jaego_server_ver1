package org.example.jaego.Dto;

import lombok.*;

// 재고(출고) 엑셀 데이터
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExcelRowDto {
    private String productName;      // 상품명
    private Integer salesQuantity;   // 판매수량
    private Integer remainingStock;  // 남은재고수량(총수량)

    // 검증용 필드
    private boolean isValid = true;
    private String errorMessage;
}