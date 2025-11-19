package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.model.dto.ProductPerformanceDTO;
import com.linter.eduanalitycs.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMetricsService {
    private final OrderRepository orderRepository;

    public List<ProductPerformanceDTO> getProductPerformance(LocalDateTime start, LocalDateTime end) {
        log.info("Getting product performance data");
        List<Object[]> performanceData = orderRepository.getProductPerformance(start, end);

        log.info("Get product performance from repo: {}", performanceData.stream().map(Object[]::toString).collect(Collectors.joining(",")));
        return performanceData.stream()
                .limit(5)
                .map(this::mapToProductPerformanceDTO)
                .collect(Collectors.toList());
    }

    private ProductPerformanceDTO mapToProductPerformanceDTO(Object[] data) {
        Long courseId = (Long) data[0];
        String courseName = (String) data[1];
        Long salesCount = (Long) data[2];
        BigDecimal revenue = (BigDecimal) data[3];

        return new ProductPerformanceDTO(courseId, courseName, salesCount, revenue);
    }
}