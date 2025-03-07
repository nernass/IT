package com.hashcodehub.springboot.studentservices.integration;

import com.hashcodehub.springboot.studentservices.Controller.StudentController;
import com.hashcodehub.springboot.studentservices.Model.Course;
import com.hashcodehub.springboot.studentservices.Model.Student;
import com.hashcodehub.springboot.studentservices.Service.StudentService;
import com.hashcodehub.springboot.studentservices.StudentServicesApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = StudentServicesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentController studentController;

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private Course testCourse;
    private static final String STUDENT_ID = "Student1";

    @BeforeEach
    void setUp() {
        testCourse = new Course(
                "TestCourse",
                "Integration Testing",
                "Test Description",
                Arrays.asList("Step 1", "Step 2", "Step 3"));
    }

    @Test
    void testEndToEndCourseCreationAndRetrieval() {
        // 1. Create a new course for a student
        String url = createURLWithPort("/students/" + STUDENT_ID + "/courses");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Course> request = new HttpEntity<>(testCourse, headers);

        ResponseEntity<Void> createResponse = restTemplate.postForEntity(url, request, Void.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

        // 2. Retrieve all courses for the student
        List<Course> courses = studentService.retreiveCourses(STUDENT_ID);
        assertNotNull(courses);
        assertTrue(courses.size() > 0);

        // Verify the course was added
        Course addedCourse = courses.stream()
                .filter(c -> c.getName().equals(testCourse.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(addedCourse);
        assertEquals(testCourse.getDescription(), addedCourse.getDescription());
    }

    @Test
    void testCourseRetrievalForNonExistentStudent() {
        String nonExistentStudentId = "NonExistentStudent";
        List<Course> courses = studentService.retreiveCourses(nonExistentStudentId);
        assertNull(courses);
    }

    @Test
    void testStudentAndCourseInteraction() {
        // 1. First verify student exists
        Student student = studentService.retreiveStudent(STUDENT_ID);
        assertNotNull(student);

        // 2. Add new course
        Course addedCourse = studentService.addCourse(STUDENT_ID, testCourse);
        assertNotNull(addedCourse);
        assertNotNull(addedCourse.getId()); // Verify ID was generated

        // 3. Verify course was added through controller
        Course retrievedCourse = studentController.retreiveDetailsForCourse(
                STUDENT_ID,
                addedCourse.getId());
        assertNotNull(retrievedCourse);
        assertEquals(testCourse.getName(), retrievedCourse.getName());
        assertEquals(testCourse.getDescription(), retrievedCourse.getDescription());
    }

    @Test
    void testDataConsistencyAcrossComponents() {
        // 1. Add course through service
        Course addedCourse = studentService.addCourse(STUDENT_ID, testCourse);
        assertNotNull(addedCourse);

        // 2. Verify through controller
        List<Course> coursesFromController = studentController.retreiveCoursesForStudent(STUDENT_ID);
        assertNotNull(coursesFromController);

        // 3. Verify through service
        List<Course> coursesFromService = studentService.retreiveCourses(STUDENT_ID);
        assertNotNull(coursesFromService);

        // 4. Compare results
        assertEquals(coursesFromController.size(), coursesFromService.size());

        Course courseFromController = coursesFromController.stream()
                .filter(c -> c.getId().equals(addedCourse.getId()))
                .findFirst()
                .orElse(null);

        Course courseFromService = coursesFromService.stream()
                .filter(c -> c.getId().equals(addedCourse.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(courseFromController);
        assertNotNull(courseFromService);
        assertEquals(courseFromController, courseFromService);
    }
}