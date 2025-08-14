package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ExpirationRiskDto {
    private Long highRiskCount;
    private Long mediumRiskCount;
    private Long lowRiskCount;
    private List<UrgentBatchDto> highRiskProducts;
    private List<UrgentBatchDto> mediumRiskProducts;
    private List<UrgentBatchDto> lowRiskProducts;
}
