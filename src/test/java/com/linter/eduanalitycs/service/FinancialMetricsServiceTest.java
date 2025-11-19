package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.repository.OrderRepository;
import com.linter.eduanalitycs.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialMetricsService Tests")
class FinancialMetricsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private FinancialMetricsService financialMetricsService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
    }

    @Test
    @DisplayName("Should calculate LTV correctly")
    void shouldCalculateLTVCorrectly() {
        // Given
        BigDecimal totalRevenue = new BigDecimal("1000000");
        long totalUsers = 100L;

        when(orderRepository.getTotalRevenue()).thenReturn(totalRevenue);
        when(userRepository.count()).thenReturn(totalUsers);

        // When
        BigDecimal ltv = financialMetricsService.getLTV();

        // Then
        assertNotNull(ltv);
        assertEquals(new BigDecimal("10000.00"), ltv);
        verify(orderRepository, times(1)).getTotalRevenue();
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should return zero LTV when no users")
    void shouldReturnZeroLTVWhenNoUsers() {
        // Given
        when(orderRepository.getTotalRevenue()).thenReturn(new BigDecimal("1000000"));
        when(userRepository.count()).thenReturn(0L);

        // When
        BigDecimal ltv = financialMetricsService.getLTV();

        // Then
        assertNotNull(ltv);
        assertEquals(BigDecimal.ZERO, ltv);
    }

    @Test
    @DisplayName("Should calculate CAC correctly")
    void shouldCalculateCACCorrectly() {
        // Given
        long newUsers = 50L;
        // 30 days period, so costs = 50000 * 30 / 30 = 50000
        when(userRepository.countByRegistrationDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(newUsers);

        // When
        BigDecimal cac = financialMetricsService.getCAC(startDate, endDate);

        // Then
        assertNotNull(cac);
        // Expected: 50000 / 50 = 1000
        assertEquals(new BigDecimal("1000.00"), cac);
        verify(userRepository, times(1)).countByRegistrationDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should return zero CAC when no new users")
    void shouldReturnZeroCACWhenNoNewUsers() {
        // Given
        when(userRepository.countByRegistrationDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        // When
        BigDecimal cac = financialMetricsService.getCAC(startDate, endDate);

        // Then
        assertNotNull(cac);
        assertEquals(BigDecimal.ZERO, cac);
    }

    @Test
    @DisplayName("Should calculate ARPPU correctly")
    void shouldCalculateARPPUCorrectly() {
        // Given
        BigDecimal periodRevenue = new BigDecimal("500000");
        long payingUsers = 100L;

        when(orderRepository.getTotalRevenueBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(periodRevenue);
        when(orderRepository.countDistinctPayingUsersBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(payingUsers);

        // When
        BigDecimal arppu = financialMetricsService.getARPPU(startDate, endDate);

        // Then
        assertNotNull(arppu);
        assertEquals(new BigDecimal("5000.00"), arppu);
        verify(orderRepository, times(1)).getTotalRevenueBetween(startDate, endDate);
        verify(orderRepository, times(1)).countDistinctPayingUsersBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should return zero ARPPU when no paying users")
    void shouldReturnZeroARPPUWhenNoPayingUsers() {
        // Given
        when(orderRepository.getTotalRevenueBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("500000"));
        when(orderRepository.countDistinctPayingUsersBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        // When
        BigDecimal arppu = financialMetricsService.getARPPU(startDate, endDate);

        // Then
        assertNotNull(arppu);
        assertEquals(BigDecimal.ZERO, arppu);
    }

    @Test
    @DisplayName("Should calculate period costs correctly for different periods")
    void shouldCalculatePeriodCostsCorrectly() {
        // Given - 15 days period
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 16, 0, 0);
        long newUsers = 10L;

        when(userRepository.countByRegistrationDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(newUsers);

        // When
        BigDecimal cac = financialMetricsService.getCAC(start, end);

        // Then
        assertNotNull(cac);
        // Expected: (50000 * 15 / 30) / 10 = 25000 / 10 = 2500
        assertEquals(new BigDecimal("2500.00"), cac);
    }
}


