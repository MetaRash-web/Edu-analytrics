package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.model.dto.ProductPerformanceDTO;
import com.linter.eduanalitycs.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductMetricsService Tests")
class ProductMetricsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ProductMetricsService productMetricsService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
    }

    @Test
    @DisplayName("Should return product performance list")
    void shouldReturnProductPerformance() {
        // Given
        Object[] data1 = new Object[]{1L, "Java Основы", 50L, new BigDecimal("250000")};
        Object[] data2 = new Object[]{2L, "Python для начинающих", 30L, new BigDecimal("120000")};
        Object[] data3 = new Object[]{3L, "Data Science Intro", 20L, new BigDecimal("120000")};
        List<Object[]> mockData = Arrays.asList(data1, data2, data3);

        when(orderRepository.getProductPerformance(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        List<ProductPerformanceDTO> result = productMetricsService.getProductPerformance(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        ProductPerformanceDTO first = result.get(0);
        assertEquals(1L, first.getCourseId());
        assertEquals("Java Основы", first.getCourseName());
        assertEquals(50L, first.getSalesCount());
        assertEquals(new BigDecimal("250000"), first.getRevenue());

        verify(orderRepository, times(1)).getProductPerformance(startDate, endDate);
    }

    @Test
    @DisplayName("Should limit results to 5 products")
    void shouldLimitResultsToFive() {
        // Given
        List<Object[]> mockData = Arrays.asList(
                new Object[]{1L, "Course 1", 100L, new BigDecimal("500000")},
                new Object[]{2L, "Course 2", 90L, new BigDecimal("450000")},
                new Object[]{3L, "Course 3", 80L, new BigDecimal("400000")},
                new Object[]{4L, "Course 4", 70L, new BigDecimal("350000")},
                new Object[]{5L, "Course 5", 60L, new BigDecimal("300000")},
                new Object[]{6L, "Course 6", 50L, new BigDecimal("250000")},
                new Object[]{7L, "Course 7", 40L, new BigDecimal("200000")}
        );

        when(orderRepository.getProductPerformance(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        List<ProductPerformanceDTO> result = productMetricsService.getProductPerformance(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should return empty list when no data")
    void shouldReturnEmptyListWhenNoData() {
        // Given
        when(orderRepository.getProductPerformance(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // When
        List<ProductPerformanceDTO> result = productMetricsService.getProductPerformance(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).getProductPerformance(startDate, endDate);
    }

    @Test
    @DisplayName("Should map data correctly to DTO")
    void shouldMapDataCorrectlyToDTO() {
        // Given
        Object[] data = new Object[]{10L, "Test Course", 25L, new BigDecimal("125000")};
        List<Object[]> mockData = new ArrayList<>();
        mockData.add(data);

        when(orderRepository.getProductPerformance(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockData);

        // When
        List<ProductPerformanceDTO> result = productMetricsService.getProductPerformance(startDate, endDate);

        // Then
        assertEquals(1, result.size());
        ProductPerformanceDTO dto = result.get(0);
        assertEquals(10L, dto.getCourseId());
        assertEquals("Test Course", dto.getCourseName());
        assertEquals(25L, dto.getSalesCount());
        assertEquals(new BigDecimal("125000"), dto.getRevenue());
    }
}

