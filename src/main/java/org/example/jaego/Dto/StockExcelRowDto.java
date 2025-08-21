package org.example.jaego.Dto;
import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockExcelRowDto {
    private String productName;      // 상품명
    private Integer salesQuantity;   // 판매수량(빼야할 수량)
    private Integer remainingStock;  // 총수량(남겨야 할 수량)

    @Builder.Default
    private boolean isValid = true;  // 검증 여부
    private String errorMessage;     // 검증 실패 메시지
}
