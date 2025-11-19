package com.linter.eduanalitycs.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CompleteMetricsResponse {
    private DashboardStats dashboardStats;
    private Map<String, Map<LocalDate, Integer>> audienceMetrics;
    private double retentionRate;
    private BigDecimal ltv;
    private BigDecimal cac;
    private BigDecimal arppu;
    private List<ProductPerformanceDTO> productPerformance;
    private Map<LocalDate, Double> retentionTrend;
}