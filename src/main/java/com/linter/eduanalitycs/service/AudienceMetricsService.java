package com.linter.eduanalitycs.service;

import com.linter.eduanalitycs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AudienceMetricsService {
    private final UserRepository userRepository;

    public Map<String, Map<LocalDate, Integer>> getAudienceMetrics(LocalDateTime start, LocalDateTime end, String aggregation) {
        List<Object[]> dailyActive = userRepository.getDailyActiveUsers(start, end);
        Map<LocalDate, Set<Long>> dailyActiveMap = mapDailyActiveUsers(dailyActive);

        // Агрегируем сырые данные, если нужно
        Map<LocalDate, Set<Long>> aggregatedMap = aggregateDailyData(dailyActiveMap, aggregation);

        return calculateAudienceMetrics(aggregatedMap);
    }

    // Новый метод: агрегация по 'week' или 'month' (для 'day' — ничего не меняет)
    private Map<LocalDate, Set<Long>> aggregateDailyData(Map<LocalDate, Set<Long>> dailyMap, String aggregation) {
        if ("day".equalsIgnoreCase(aggregation)) {
            return dailyMap;
        }

        Map<LocalDate, Set<Long>> aggregated = new TreeMap<>();
        for (Map.Entry<LocalDate, Set<Long>> entry : dailyMap.entrySet()) {
            LocalDate date = entry.getKey();
            LocalDate aggKey = switch (aggregation.toLowerCase()) {
                case "week" -> date.minusDays(date.getDayOfWeek().getValue() - 1); // Понедельник недели
                case "month" -> date.withDayOfMonth(1); // 1-е число месяца
                default -> date;
            };

            aggregated.computeIfAbsent(aggKey, k -> new HashSet<>()).addAll(entry.getValue());
        }
        return aggregated;
    }

    private Map<LocalDate, Set<Long>> mapDailyActiveUsers(List<Object[]> dailyActive) {
        Map<LocalDate, Set<Long>> dailyActiveMap = new HashMap<>();
        for (Object[] result : dailyActive) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Long userId = ((Number) result[1]).longValue();
            dailyActiveMap.computeIfAbsent(date, k -> new HashSet<>()).add(userId);
        }
        return dailyActiveMap;
    }

    private Map<String, Map<LocalDate, Integer>> calculateAudienceMetrics(Map<LocalDate, Set<Long>> dailyActive) {
        return Map.of(
                "DAU", calculateDAU(dailyActive),
                "WAU", calculateWAU(dailyActive),
                "MAU", calculateMAU(dailyActive)
        );
    }

    private Map<LocalDate, Integer> calculateDAU(Map<LocalDate, Set<Long>> dailyActive) {
        Map<LocalDate, Integer> dauMap = new TreeMap<>();
        dailyActive.forEach((date, users) -> dauMap.put(date, users.size()));
        return dauMap;
    }

    private Map<LocalDate, Integer> calculateWAU(Map<LocalDate, Set<Long>> dailyActive) {
        return calculateRollingMetric(dailyActive, 7);
    }

    private Map<LocalDate, Integer> calculateMAU(Map<LocalDate, Set<Long>> dailyActive) {
        return calculateRollingMetric(dailyActive, 30);
    }

    private Map<LocalDate, Integer> calculateRollingMetric(Map<LocalDate, Set<Long>> dailyActive, int days) {
        Map<LocalDate, Integer> result = new TreeMap<>();
        List<LocalDate> dates = new ArrayList<>(dailyActive.keySet());
        Collections.sort(dates);

        Set<Long> currentWindow = new HashSet<>();
        Deque<LocalDate> dateWindow = new ArrayDeque<>();

        for (LocalDate date : dates) {
            // Удаляем устаревшие даты из окна
            while (!dateWindow.isEmpty() && dateWindow.peekFirst().isBefore(date.minusDays(days - 1))) {
                LocalDate oldDate = dateWindow.removeFirst();
                currentWindow.removeAll(dailyActive.get(oldDate));
            }

            dateWindow.addLast(date);
            currentWindow.addAll(dailyActive.get(date));

            result.put(date, currentWindow.size());
        }
        return result;
    }
}
