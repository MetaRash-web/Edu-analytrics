package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.repository.OrderRepository;
import com.linter.eduanalitycs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class FinancialMetricsService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private static final BigDecimal MONTHLY_MARKETING_COSTS = new BigDecimal("50000");

    public BigDecimal getLTV() {
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        long totalUsers = userRepository.count();

        return totalUsers > 0 ?
                totalRevenue.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    public BigDecimal getCAC(LocalDateTime start, LocalDateTime end) {
        long newUsersInPeriod = userRepository.countByRegistrationDateBetween(start, end);
        BigDecimal periodCosts = calculatePeriodCosts(start, end);

        return newUsersInPeriod > 0 ?
                periodCosts.divide(BigDecimal.valueOf(newUsersInPeriod), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    public BigDecimal getARPPU(LocalDateTime start, LocalDateTime end) {
        BigDecimal periodRevenue = orderRepository.getTotalRevenueBetween(start, end);
        long payingUsers = orderRepository.countDistinctPayingUsersBetween(start, end);

        return payingUsers > 0 ?
                periodRevenue.divide(BigDecimal.valueOf(payingUsers), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    private BigDecimal calculatePeriodCosts(LocalDateTime start, LocalDateTime end) {
        long daysInPeriod = ChronoUnit.DAYS.between(start, end);
        return MONTHLY_MARKETING_COSTS
                .multiply(BigDecimal.valueOf(daysInPeriod))
                .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
    }
}