package com.linter.eduanalitycs.repository;

import com.linter.eduanalitycs.model.entity.Course;
import com.linter.eduanalitycs.model.entity.Order;
import com.linter.eduanalitycs.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User user1;
    private User user2;
    private Course course1;
    private Course course2;
    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = new User(null, "User 1", LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(1), null);
        user2 = new User(null, "User 2", LocalDateTime.now().minusDays(20), LocalDateTime.now(), null);
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);

        // Create courses
        course1 = new Course(null, "Java Basics", new BigDecimal("5000"));
        course2 = new Course(null, "Python Advanced", new BigDecimal("8000"));
        course1 = courseRepository.save(course1);
        course2 = courseRepository.save(course2);

        // Create orders
        LocalDateTime now = LocalDateTime.now();
        order1 = new Order(null, user1, course1, now.minusDays(10), new BigDecimal("5000"));
        order2 = new Order(null, user1, course2, now.minusDays(5), new BigDecimal("8000"));
        order3 = new Order(null, user2, course1, now.minusDays(2), new BigDecimal("5000"));

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        order3 = orderRepository.save(order3);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should find orders by date range")
    void shouldFindOrdersByDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<Order> orders = orderRepository.findByOrderDateBetween(start, end);

        // Then
        assertNotNull(orders);
        assertTrue(orders.size() >= 3);
    }

    @Test
    @DisplayName("Should count orders by date range")
    void shouldCountOrdersByDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        long count = orderRepository.countByOrderDateBetween(start, end);

        // Then
        assertTrue(count >= 3);
    }

    @Test
    @DisplayName("Should get total revenue")
    void shouldGetTotalRevenue() {
        // When
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();

        // Then
        assertNotNull(totalRevenue);
        assertEquals(0, new BigDecimal("18000").compareTo(totalRevenue));
    }

    @Test
    @DisplayName("Should get total revenue between dates")
    void shouldGetTotalRevenueBetweenDates() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        BigDecimal totalRevenue = orderRepository.getTotalRevenueBetween(start, end);

        // Then
        assertNotNull(totalRevenue);
        assertEquals(0, new BigDecimal("18000").compareTo(totalRevenue));
    }

    @Test
    @DisplayName("Should count distinct paying users between dates")
    void shouldCountDistinctPayingUsersBetweenDates() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        long payingUsers = orderRepository.countDistinctPayingUsersBetween(start, end);

        // Then
        assertEquals(2, payingUsers); // user1 and user2
    }

    @Test
    @DisplayName("Should get product performance")
    void shouldGetProductPerformance() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<Object[]> performance = orderRepository.getProductPerformance(start, end);

        // Then
        assertNotNull(performance);
        assertTrue(performance.size() >= 2);

        // Check first product (should be course1 with 2 orders)
        Object[] firstProduct = performance.get(0);
        assertNotNull(firstProduct);
        assertEquals(2, firstProduct.length >= 3 ? ((Number) firstProduct[2]).longValue() : 0);
    }

    @Test
    @DisplayName("Should find orders by user")
    void shouldFindOrdersByUser() {
        // When
        List<Order> orders = orderRepository.findByUser(user1);

        // Then
        assertNotNull(orders);
        assertEquals(2, orders.size());
    }

    @Test
    @DisplayName("Should return zero revenue when no orders")
    void shouldReturnZeroRevenueWhenNoOrders() {
        // Given - query for future date range with no orders
        LocalDateTime futureStart = LocalDateTime.now().plusDays(10);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(20);
        
        // When
        BigDecimal revenue = orderRepository.getTotalRevenueBetween(futureStart, futureEnd);

        // Then
        assertNotNull(revenue);
        assertEquals(BigDecimal.ZERO, revenue);
    }
}

