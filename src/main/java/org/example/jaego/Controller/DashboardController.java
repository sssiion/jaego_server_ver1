package org.example.jaego.Controller;


import lombok.RequiredArgsConstructor;

import org.example.jaego.Service.DashboardService;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDto getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/categories")
    public List<CategoryStatsDto> getCategoryStats() {
        return dashboardService.getCategoryStats();
    }

    @GetMapping("/urgent")
    public UrgentProductsStatsDto getUrgentProductsStats(@RequestParam(defaultValue = "7") Integer days) {
        return dashboardService.getUrgentProductsStats(days);
    }

    @GetMapping("/expiry-trend")
    public List<ExpiryTrendDto> getExpiryTrendData(@RequestParam String start, @RequestParam String end) {
        return dashboardService.getExpiryTrendData(LocalDateTime.parse(start), LocalDateTime.parse(end));
    }

    @GetMapping("/top-urgent")
    public List<UrgentInventoryDto> getTopUrgentProducts(@RequestParam(defaultValue = "5") Integer limit) {
        return dashboardService.getTopUrgentProducts(limit);
    }
}
