package com.linter.eduanalitycs.config;

import com.linter.eduanalitycs.model.entity.Course;
import com.linter.eduanalitycs.model.entity.Order;
import com.linter.eduanalitycs.model.entity.User;
import com.linter.eduanalitycs.repository.CourseRepository;
import com.linter.eduanalitycs.repository.OrderRepository;
import com.linter.eduanalitycs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || courseRepository.count() > 0 || orderRepository.count() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        // === КУРСЫ ===
        List<Course> courses = createCourses(Map.ofEntries(
                entry("История музыки за 15 минут в день. Балкон", 27900),
                entry("История музыки за 15 минут в день. Партер", 56900),
                entry("История музыки за 15 минут в день. Ложа", 134780),
                entry("Музыка и литература.", 12900),
                entry("Музыка и живопись", 5900),
                entry("Музыка и литература + живопись + путешествия", 15900),
                entry("Великие композиторы", 27000),
                entry("Как устроена музыка", 15000),
                entry("Великие композиторы и Как устроена музыка", 42000),
                entry("Тайны 24-ч тональностей", 10000),
                entry("Русская классика для детей. Вершки", 10000),
                entry("Русская классика для детей. Вершки и корешки", 20000),
                entry("Мой друг Моцарт 2.0. Виртуоз", 55000),
                entry("Мой друг Моцарт 3.0. История искусств", 85000),
                entry("Семейный просмотр", 141900),
                entry("Музыка и рисунки", 10000)
        ));
        courseRepository.saveAllAndFlush(courses);

        ThreadLocalRandom r = ThreadLocalRandom.current();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);

        List<User> users = new ArrayList<>();
        List<Order> orders = new ArrayList<>();

        // === Генерируем 500 пользователей (чтобы статистика была гладкая) ===
        for (int i = 0; i < 500; i++) {
            LocalDateTime regDate = randomDate(oneYearAgo, now, r);

            User user = new User();
            user.setName("User" + (1000 + i));
            user.setRegistrationDate(regDate);
            user.setOrders(new ArrayList<>());
            users.add(user);

            // === Симулируем поведение реального пользователя ===

            // 30% — зарегистрировались и ушли навсегда (churn сразу)
            if (r.nextDouble() < 0.30) {
                user.setLastActivityDate(regDate.plusHours(r.nextInt(24)));
                continue;
            }

            // 40% — купили в первые 3 дня, потом иногда возвращаются
            if (r.nextDouble() < 0.60) { // пересекается с предыдущим условием
                LocalDateTime firstOrderDate = regDate.plusDays(r.nextLong(0, 4));
                Course course = randomCourseWeighted(courses, r); // популярные курсы чаще
                createOrder(user, course, firstOrderDate, orders);
            }

            // 25% — купили через неделю-две (отложенная конверсия)
            if (r.nextDouble() < 0.25) {
                LocalDateTime delayedOrder = regDate.plusDays(7 + r.nextInt(30));
                if (delayedOrder.isBefore(now)) {
                    createOrder(user, randomCourseWeighted(courses, r), delayedOrder, orders);
                }
            }

            // Повторные покупки (10–15% пользователей)
            if (r.nextDouble() < 0.12) {
                LocalDateTime secondOrder = regDate.plusDays(20 + r.nextInt(120));
                if (secondOrder.isBefore(now)) {
                    createOrder(user, randomCourseWeighted(courses, r), secondOrder, orders);
                }
            }

            // Обновляем lastActivityDate — последний заказ или + случайная активность
            LocalDateTime lastAct = user.getOrders().stream()
                    .map(Order::getOrderDate)
                    .max(LocalDateTime::compareTo)
                    .orElse(regDate);

            // 70% пользователей возвращаются просто 1–5 раз просто "погулять" после покупки
            if (!user.getOrders().isEmpty() && r.nextDouble() < 0.70) {
                int extraVisits = r.nextInt(1, 6);
                for (int v = 0; v < extraVisits; v++) {
                    LocalDateTime visit = lastAct.plusDays(r.nextInt(1, 90));
                    if (visit.isBefore(now)) {
                        lastAct = visit;
                    }
                }
            }

            user.setLastActivityDate(lastAct.plusMinutes(r.nextInt(120))); // финальный заход
        }

        userRepository.saveAll(users);
        orderRepository.saveAll(orders);

        log.info("Realistic test data initialized: {} users, {} orders", users.size(), orders.size());
    }

    // === Вспомогательные методы ===
    private LocalDateTime randomDate(LocalDateTime start, LocalDateTime end, ThreadLocalRandom r) {
        long days = ChronoUnit.DAYS.between(start, end);
        return start.plusDays(r.nextLong(days + 1))
                .plusHours(r.nextInt(24))
                .plusMinutes(r.nextInt(60));
    }

    private Course randomCourseWeighted(List<Course> courses, ThreadLocalRandom r) {
        // Делаем популярные курсы чаще: веса от 1 до 10
        List<Course> weighted = new ArrayList<>();
        for (Course c : courses) {
            int weight = switch (c.getName()) {
                case "История музыки за 15 минут в день. Балкон",
                     "Мой друг Моцарт 3.0. История искусств",
                     "Семейный просмотр" -> 10;
                case "Как устроена музыка", "Великие композиторы" -> 7;
                default -> 3;
            };
            for (int i = 0; i < weight; i++) weighted.add(c);
        }
        return weighted.get(r.nextInt(weighted.size()));
    }

    private void createOrder(User user, Course course, LocalDateTime date, List<Order> orders) {
        if (date.isAfter(LocalDateTime.now())) return;
        Order order = new Order(null, user, course, date, course.getPrice());
        user.getOrders().add(order);
        orders.add(order);
    }

    private List<Course> createCourses(Map<String, Object> courseData) {
        List<Course> courses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : courseData.entrySet()) {
            String name = entry.getKey();
            Object priceValue = entry.getValue();
            BigDecimal price = switch (priceValue) {
                case String s -> new BigDecimal(s);
                case Integer i -> new BigDecimal(i);
                case Double v -> BigDecimal.valueOf(v);
                default -> throw new IllegalArgumentException("Unsupported price type: " + priceValue.getClass());
            };

            courses.add(new Course(null, name, price));
        }
        return courses;
    }
}