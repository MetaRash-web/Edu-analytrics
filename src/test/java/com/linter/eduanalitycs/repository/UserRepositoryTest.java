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
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User user1;
    private User user2;
    private User user3;
    private Course course1;

    @BeforeEach
    void setUp() {
        // Create users
        LocalDateTime now = LocalDateTime.now();
        user1 = new User(null, "User 1", now.minusDays(30), now.minusDays(1), null);
        user2 = new User(null, "User 2", now.minusDays(20), now.minusDays(2), null);
        user3 = new User(null, "User 3", now.minusDays(10), now, null);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);

        // Create course
        course1 = new Course(null, "Java Basics", new BigDecimal("5000"));
        course1 = courseRepository.save(course1);

        // Create orders
        Order order1 = new Order(null, user1, course1, now.minusDays(5), new BigDecimal("5000"));
        Order order2 = new Order(null, user2, course1, now.minusDays(3), new BigDecimal("5000"));
        orderRepository.save(order1);
        orderRepository.save(order2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should count users by last activity date between")
    void shouldCountUsersByLastActivityDateBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        // When
        long count = userRepository.countByLastActivityDateBetween(start, end);

        // Then
        assertTrue(count >= 1); // user3 should be in range
    }

    @Test
    @DisplayName("Should count users by registration date between")
    void shouldCountUsersByRegistrationDateBetween() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(15);
        LocalDateTime end = LocalDateTime.now();

        // When
        long count = userRepository.countByRegistrationDateBetween(start, end);

        // Then
        assertTrue(count >= 1); // user3 should be in range
    }

    @Test
    @DisplayName("Should get daily active users")
    void shouldGetDailyActiveUsers() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<Object[]> dailyActive = userRepository.getDailyActiveUsers(start, end);

        // Then
        assertNotNull(dailyActive);
        // Should return data if users have activity in range
    }

    @Test
    @DisplayName("Should find users with orders in period")
    void shouldFindUsersWithOrdersInPeriod() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();

        // When
        List<User> users = userRepository.findUsersWithOrdersInPeriod(start, end);

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2); // user1 and user2 have orders
    }

    @Test
    @DisplayName("Should find users with any orders before date")
    void shouldFindUsersWithAnyOrdersBeforeDate() {
        // Given
        LocalDateTime end = LocalDateTime.now();

        // When
        List<User> users = userRepository.findUsersWithAnyOrdersBefore(end);

        // Then
        assertNotNull(users);
        assertTrue(users.size() >= 2); // user1 and user2 have orders
    }

    @Test
    @DisplayName("Should return empty list when no users match criteria")
    void shouldReturnEmptyListWhenNoUsersMatchCriteria() {
        // Given
        LocalDateTime futureStart = LocalDateTime.now().plusDays(10);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(20);

        // When
        long count = userRepository.countByLastActivityDateBetween(futureStart, futureEnd);

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should handle users without orders")
    void shouldHandleUsersWithoutOrders() {
        // Given
        User userWithoutOrders = new User(null, "User Without Orders", 
                LocalDateTime.now().minusDays(5), LocalDateTime.now(), null);
        userWithoutOrders = userRepository.save(userWithoutOrders);

        LocalDateTime end = LocalDateTime.now();

        // When
        List<User> users = userRepository.findUsersWithAnyOrdersBefore(end);

        // Then
        assertNotNull(users);
        // userWithoutOrders should not be in the list
        assertFalse(users.contains(userWithoutOrders));
    }
}


