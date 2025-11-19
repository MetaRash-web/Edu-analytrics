package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.model.entity.Order;
import com.linter.eduanalitycs.model.entity.User;
import com.linter.eduanalitycs.repository.OrderRepository;
import com.linter.eduanalitycs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionMetricsService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public double getRetentionRate(LocalDateTime start, LocalDateTime end) {
        List<User> allUsersEver = userRepository.findUsersWithAnyOrdersBefore(end);
        int totalUsers = allUsersEver.size();
        if (totalUsers == 0) return 0.0;

        // Пользователи с повторными заказами (хотя бы 2 заказа всего)
        int retained = 0;
        for (User user : allUsersEver) {
            List<Order> orders = orderRepository.findByUser(user);
            if (orders.size() > 1) {
                retained++;
            }
        }
        double result = (double) retained / totalUsers * 100;
        log.info("Retention rate (RPR) is {}", result);
        return result;
    }

    public Map<LocalDate, Double> getRetentionTrend(LocalDateTime start, LocalDateTime end, String aggregation) {
        List<Object[]> retentionData = ("week".equalsIgnoreCase(aggregation) || "day".equalsIgnoreCase(aggregation))
                ? userRepository.getWeeklyRetentionTrend(start, end)
                : userRepository.getMonthlyRetentionTrend(start, end); // Новый запрос для месяца

        Map<LocalDate, Double> trend = new TreeMap<>();

        if (retentionData.isEmpty()) {
            log.warn("No retention trend data for period {} to {}", start, end);
            return trend;
        }

        for (Object[] data : retentionData) {
            if (data.length < 2 || data[0] == null || data[1] == null) {
                log.warn("Invalid data in retention trend row: {}", Arrays.toString(data));
                continue;
            }
            LocalDate periodStart = convertToLocalDate(data[0]);
            Double retentionRate = convertToDouble(data[1]);
            trend.put(periodStart, retentionRate);
        }

        log.info("Retention trend calculated: {}", trend);

        return trend;
    }

    private LocalDate convertToLocalDate(Object dateObject) {
        if (dateObject instanceof java.sql.Date) {
            return ((java.sql.Date) dateObject).toLocalDate();
        } else if (dateObject instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObject).toLocalDateTime().toLocalDate();
        } else if (dateObject instanceof java.util.Date) {
            return ((java.util.Date) dateObject).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (dateObject instanceof LocalDate) {
            return (LocalDate) dateObject;
        } else {
            throw new IllegalArgumentException("Unsupported date type: " + dateObject.getClass());
        }
    }

    private Double convertToDouble(Object number) {
        if (number == null) return 0.0;

        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).doubleValue();
        } else if (number instanceof Double) {
            return (Double) number;
        } else if (number instanceof Float) {
            return ((Float) number).doubleValue();
        } else if (number instanceof Integer) {
            return ((Integer) number).doubleValue();
        } else if (number instanceof Long) {
            return ((Long) number).doubleValue();
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + number.getClass());
        }
    }
}