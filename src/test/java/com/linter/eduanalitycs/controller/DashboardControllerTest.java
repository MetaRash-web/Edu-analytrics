package com.linter.eduanalitycs.controller;

import com.linter.eduanalitycs.model.dto.CompleteMetricsResponse;
import com.linter.eduanalitycs.model.dto.DashboardStats;
import com.linter.eduanalitycs.model.dto.ProductPerformanceDTO;
import com.linter.eduanalitycs.service.MetricsFacadeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsFacadeService metricsFacadeService;

    private CompleteMetricsResponse mockResponse;

    @BeforeEach
    void setUp() {
        DashboardStats stats = new DashboardStats(100L, 5L, 250L, new BigDecimal("1250000"));
        Map<String, Map<LocalDate, Integer>> audienceMetrics = Map.of(
                "DAU", Map.of(LocalDate.now(), 50),
                "WAU", Map.of(LocalDate.now(), 200),
                "MAU", Map.of(LocalDate.now(), 800)
        );
        List<ProductPerformanceDTO> productPerformance = Arrays.asList(
                new ProductPerformanceDTO(1L, "Course 1", 100L, new BigDecimal("500000"))
        );
        Map<LocalDate, Double> retentionTrend = Map.of(LocalDate.now(), 75.5);

        mockResponse = CompleteMetricsResponse.builder()
                .dashboardStats(stats)
                .audienceMetrics(audienceMetrics)
                .retentionRate(75.5)
                .ltv(new BigDecimal("10000.00"))
                .cac(new BigDecimal("500.00"))
                .arppu(new BigDecimal("5000.00"))
                .productPerformance(productPerformance)
                .retentionTrend(retentionTrend)
                .build();
    }

    @Test
    @DisplayName("Should return dashboard page")
    void shouldReturnDashboardPage() throws Exception {
        // Given
        when(metricsFacadeService.getCompleteMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("audienceMetrics"))
                .andExpect(model().attributeExists("retentionRate"))
                .andExpect(model().attributeExists("ltv"))
                .andExpect(model().attributeExists("cac"))
                .andExpect(model().attributeExists("arppu"))
                .andExpect(model().attributeExists("productPerformance"))
                .andExpect(model().attributeExists("retentionTrend"))
                .andExpect(model().attributeExists("userCount"))
                .andExpect(model().attributeExists("courseCount"))
                .andExpect(model().attributeExists("orderCount"))
                .andExpect(model().attributeExists("totalRevenue"))
                .andExpect(model().attributeExists("selectedPeriod"));
    }

    @Test
    @DisplayName("Should handle different periods")
    void shouldHandleDifferentPeriods() throws Exception {
        // Given
        when(metricsFacadeService.getCompleteMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        // When & Then - last7days
        mockMvc.perform(get("/").param("period", "last7days"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("selectedPeriod", "last7days"));

        // When & Then - last90days
        mockMvc.perform(get("/").param("period", "last90days"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedPeriod", "last90days"));

        // When & Then - last365days
        mockMvc.perform(get("/").param("period", "last365days"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedPeriod", "last365days"));
    }

    @Test
    @DisplayName("Should default to last30days when period not specified")
    void shouldDefaultToLast30DaysWhenPeriodNotSpecified() throws Exception {
        // Given
        when(metricsFacadeService.getCompleteMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedPeriod", "last30days"));
    }

    @Test
    @DisplayName("Should return metrics API response")
    void shouldReturnMetricsApiResponse() throws Exception {
        // Given
        when(metricsFacadeService.getCompleteMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.dashboardStats.userCount").value(100))
                .andExpect(jsonPath("$.dashboardStats.courseCount").value(5))
                .andExpect(jsonPath("$.dashboardStats.orderCount").value(250))
                .andExpect(jsonPath("$.retentionRate").value(75.5))
                .andExpect(jsonPath("$.ltv").value(10000.00))
                .andExpect(jsonPath("$.cac").value(500.00))
                .andExpect(jsonPath("$.arppu").value(5000.00))
                .andExpect(jsonPath("$.productPerformance").isArray())
                .andExpect(jsonPath("$.productPerformance[0].courseId").value(1))
                .andExpect(jsonPath("$.productPerformance[0].courseName").value("Course 1"));
    }

    @Test
    @DisplayName("Should return metrics API with period parameter")
    void shouldReturnMetricsApiWithPeriodParameter() throws Exception {
        // Given
        when(metricsFacadeService.getCompleteMetrics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/metrics").param("period", "last7days"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}


