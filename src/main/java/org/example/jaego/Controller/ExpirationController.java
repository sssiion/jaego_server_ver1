package org.example.jaego.Controller;


import org.example.jaego.Dto.*;

import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.*;
import org.example.jaego.Service.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expiration")
@RequiredArgsConstructor
public class ExpirationController {

    private final ExpirationService expirationService;

    @GetMapping("/urgent")
    public List<UrgentInventoryDto> getUrgentProducts(@RequestParam String type, @RequestParam(defaultValue = "7") Integer days) {
        return expirationService.getUrgentProductsByType(type, days);
    }

    @GetMapping("/today")
    public List<ExpiredBatchDto> getExpiringToday() {
        return expirationService.getExpiringToday();
    }

    @GetMapping("/week")
    public List<UrgentBatchDto> getExpiringThisWeek() {
        return expirationService.getExpiringThisWeek();
    }

    @GetMapping("/month")
    public List<UrgentBatchDto> getExpiringThisMonth() {
        return expirationService.getExpiringThisMonth();
    }

    @PostMapping("/alerts")
    public OperationResult sendExpirationAlerts(@RequestParam(defaultValue = "7") Integer days) {
        return expirationService.sendExpirationAlerts(days);
    }

    @PostMapping("/process")
    public BatchOperationResult processExpiredProducts() {
        return expirationService.processExpiredProducts();
    }
}
