package com.linter.eduanalitycs.repository;

import com.linter.eduanalitycs.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT cast(u.lastActivityDate as date), u.id FROM User u " +
            "WHERE u.lastActivityDate BETWEEN :start AND :end " +
            "ORDER BY u.lastActivityDate")
    List<Object[]> getDailyActiveUsers(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    long countByLastActivityDateBetween(LocalDateTime start, LocalDateTime end);
    long countByRegistrationDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT o.user FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    List<User> findUsersWithOrdersInPeriod(LocalDateTime start, LocalDateTime end);

    // Тренд Retention по неделям
    @Query(value = """
        WITH cohorts AS (
            SELECT
                DATE_TRUNC('week', registration_date)::date AS cohort_week,
                COUNT(*) AS total_users,
                COUNT(*) FILTER (
                    WHERE last_activity_date >= DATE_TRUNC('week', registration_date)::date + 7
                ) AS retained_users
            FROM users
            WHERE registration_date BETWEEN :start AND :end
            GROUP BY DATE_TRUNC('week', registration_date)::date
        )
        SELECT
            cohort_week AS week_start,
            CASE 
                WHEN total_users > 0 THEN ROUND(CAST(retained_users AS DOUBLE) * 100.0 / total_users, 2)
                ELSE 0.0
            END AS retention_rate
        FROM cohorts
        ORDER BY cohort_week
        """, nativeQuery = true)
    List<Object[]> getWeeklyRetentionTrend(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
        WITH cohorts AS (
            SELECT
                DATE_TRUNC('month', registration_date)::date AS cohort_month,
                COUNT(*) AS total_users,
                COUNT(*) FILTER (
                    WHERE last_activity_date >= DATE_TRUNC('month', registration_date)::date + 7
                ) AS retained_users
            FROM users
            WHERE registration_date BETWEEN :start AND :end
            GROUP BY cohort_month
        )
        SELECT
            cohort_month AS month_start,
            CASE 
                WHEN total_users > 0 THEN ROUND(CAST(retained_users AS DOUBLE) * 100.0 / total_users, 2)
                ELSE 0.0 
            END AS retention_rate
        FROM cohorts
        ORDER BY cohort_month
        """, nativeQuery = true)
    List<Object[]> getMonthlyRetentionTrend(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o WHERE o.orderDate < :end")
    List<User> findUsersWithAnyOrdersBefore(@Param("end") LocalDateTime end);
}