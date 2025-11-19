package com.linter.eduanalitycs.repository;

import com.linter.eduanalitycs.model.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
@DisplayName("CourseRepository Tests")
class CourseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseRepository courseRepository;

    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        course1 = new Course(null, "Java Basics", new BigDecimal("5000"));
        course2 = new Course(null, "Python Advanced", new BigDecimal("8000"));

        course1 = courseRepository.save(course1);
        course2 = courseRepository.save(course2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should save and find course")
    void shouldSaveAndFindCourse() {
        // When
        Optional<Course> found = courseRepository.findById(course1.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Java Basics", found.get().getName());
        assertEquals(0, new BigDecimal("5000").compareTo(found.get().getPrice()));
    }

    @Test
    @DisplayName("Should count all courses")
    void shouldCountAllCourses() {
        // When
        long count = courseRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should find all courses")
    void shouldFindAllCourses() {
        // When
        List<Course> courses = courseRepository.findAll();

        // Then
        assertNotNull(courses);
        assertEquals(2, courses.size());
    }

    @Test
    @DisplayName("Should update course")
    void shouldUpdateCourse() {
        // Given
        course1.setPrice(new BigDecimal("6000"));
        courseRepository.save(course1);

        // When
        Optional<Course> updated = courseRepository.findById(course1.getId());

        // Then
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("6000"), updated.get().getPrice());
    }

    @Test
    @DisplayName("Should delete course")
    void shouldDeleteCourse() {
        // Given
        Long courseId = course1.getId();

        // When
        courseRepository.delete(course1);
        entityManager.flush();

        // Then
        Optional<Course> deleted = courseRepository.findById(courseId);
        assertFalse(deleted.isPresent());
        assertEquals(1, courseRepository.count());
    }
}

