package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.model.dto.CompleteMetricsResponse;
import com.linter.eduanalitycs.model.dto.DashboardStats;
import com.linter.eduanalitycs.repository.CourseRepository;
import com.linter.eduanalitycs.repository.OrderRepository;
import com.linter.eduanalitycs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MetricsFacadeService {
    private final AudienceMetricsService audienceMetricsService;
    private final FinancialMetricsService financialMetricsService;
    private final ProductMetricsService productMetricsService;
    private final RetentionMetricsService retentionMetricsService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;

    public DashboardStats getDashboardStats(LocalDateTime start, LocalDateTime end) {
        long userCount = userRepository.countByLastActivityDateBetween(start, end);
        long courseCount = courseRepository.count();
        long orderCount = orderRepository.countByOrderDateBetween(start, end);
        BigDecimal totalRevenue = orderRepository.getTotalRevenueBetween(start, end);

        return new DashboardStats(userCount, courseCount, orderCount, totalRevenue);
    }

    public CompleteMetricsResponse getCompleteMetrics(LocalDateTime start, LocalDateTime end) {
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        String agg = days > 180 ? "month" : (days >= 30 ? "week" : "day");
        return CompleteMetricsResponse.builder()
                .dashboardStats(getDashboardStats(start, end))
                .audienceMetrics(audienceMetricsService.getAudienceMetrics(start, end, agg))
                .retentionRate(retentionMetricsService.getRetentionRate(start, end))
                .ltv(financialMetricsService.getLTV())
                .cac(financialMetricsService.getCAC(start, end))
                .arppu(financialMetricsService.getARPPU(start, end))
                .productPerformance(productMetricsService.getProductPerformance(start, end))
                .retentionTrend(retentionMetricsService.getRetentionTrend(start, end, agg))
                .build();
    }
}