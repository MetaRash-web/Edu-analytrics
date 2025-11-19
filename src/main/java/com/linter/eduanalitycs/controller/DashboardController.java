package com.linter.eduanalitycs.controller;

import com.linter.eduanalitycs.model.dto.CompleteMetricsResponse;
import com.linter.eduanalitycs.model.dto.DashboardStats;
import com.linter.eduanalitycs.model.dto.PeriodInfo;
import com.linter.eduanalitycs.service.MetricsFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final MetricsFacadeService metricsFacadeService;

    @GetMapping("/")
    public String dashboard(Model model, @RequestParam(defaultValue = "last30days") String period) {
        PeriodInfo periodInfo = calculatePeriod(period);

        // Получаем все метрики за один вызов
        CompleteMetricsResponse metrics = metricsFacadeService.getCompleteMetrics(periodInfo.start(), periodInfo.end());

        // Основные метрики
        model.addAttribute("audienceMetrics", metrics.getAudienceMetrics());
        model.addAttribute("retentionRate", metrics.getRetentionRate());
        model.addAttribute("ltv", metrics.getLtv());
        model.addAttribute("cac", metrics.getCac());
        model.addAttribute("arppu", metrics.getArppu());
        model.addAttribute("productPerformance", metrics.getProductPerformance());
        model.addAttribute("retentionTrend", metrics.getRetentionTrend());

        // Общая статистика из DashboardStats
        DashboardStats stats = metrics.getDashboardStats();
        model.addAttribute("userCount", stats.getUserCount());
        model.addAttribute("courseCount", stats.getCourseCount());
        model.addAttribute("orderCount", stats.getOrderCount());
        model.addAttribute("totalRevenue", stats.getTotalRevenue());

        model.addAttribute("selectedPeriod", period);

        return "dashboard";
    }

    private PeriodInfo calculatePeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate endDate = now.toLocalDate();
        LocalDate startDate = switch (period.toLowerCase()) {
            case "last7days" -> endDate.minusDays(7);
            case "last90days" -> endDate.minusDays(90);
            case "last365days" -> endDate.minusDays(365);
            default -> endDate.minusDays(30);
        };
        return new PeriodInfo(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @GetMapping("/api/metrics")
    public ResponseEntity<CompleteMetricsResponse> getMetrics(@RequestParam(defaultValue = "last30days") String period) {
        PeriodInfo periodInfo = calculatePeriod(period);
        CompleteMetricsResponse metrics = metricsFacadeService.getCompleteMetrics(periodInfo.start(), periodInfo.end());
        return ResponseEntity.ok(metrics);
    }
}