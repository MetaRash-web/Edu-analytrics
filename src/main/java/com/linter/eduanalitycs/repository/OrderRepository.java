package com.linter.eduanalitycs.repository;

import com.linter.eduanalitycs.model.entity.Order;
import com.linter.eduanalitycs.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(o.amount) FROM Order o")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(DISTINCT o.user.id) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    long countDistinctPayingUsersBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("SELECT c.id, c.name, COUNT(o.id), SUM(o.amount) " +
            "FROM Order o JOIN o.course c " +
            "WHERE o.orderDate BETWEEN :start AND :end " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(o.id) DESC")
    List<Object[]> getProductPerformance(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    BigDecimal getTotalRevenueBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderDate < :before")
    List<Order> findByUserAndOrderDateBefore(@Param("user") User user, @Param("before") LocalDateTime before);

    List<Order> findByUser(User user);
}
