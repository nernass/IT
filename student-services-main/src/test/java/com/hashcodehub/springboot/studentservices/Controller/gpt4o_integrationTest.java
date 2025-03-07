package com.hashcodehub.springboot.studentservices;

import com.hashcodehub.springboot.studentservices.Controller.StudentController;
import com.hashcodehub.springboot.studentservices.Model.Course;
import com.hashcodehub.springboot.studentservices.Model.Student;
import com.hashcodehub.springboot.studentservices.Service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRetrieveCoursesForStudent() {
        Course course = new Course("Course1", "spring", "10 Steps",
                Arrays.asList("Learn Maven", "Import Project", "First Example", "Second Example"));
        List<Course> courses = Arrays.asList(course);
        when(studentService.retreiveCourses("Student1")).thenReturn(courses);

        ResponseEntity<Course[]> response = restTemplate.getForEntity(createURLWithPort("/students/Student1/courses"),
                Course[].class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Course1", response.getBody()[0].getId());
    }

    @Test
    public void testRetrieveDetailsForCourse() {
        Course course = new Course("Course1", "spring", "10 Steps",
                Arrays.asList("Learn Maven", "Import Project", "First Example", "Second Example"));
        when(studentService.retreiveCourse("Student1", "Course1")).thenReturn(course);

        ResponseEntity<Course> response = restTemplate
                .getForEntity(createURLWithPort("/students/Student1/courses/Course1"), Course.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Course1", response.getBody().getId());
    }

    @Test
    public void testRegisterStudentForCourse() {
        Course newCourse = new Course(null, "spring", "10 Steps",
                Arrays.asList("Learn Maven", "Import Project", "First Example", "Second Example"));
        Course createdCourse = new Course("randomId", "spring", "10 Steps",
                Arrays.asList("Learn Maven", "Import Project", "First Example", "Second Example"));
        when(studentService.addCourse("Student1", newCourse)).thenReturn(createdCourse);

        HttpEntity<Course> request = new HttpEntity<>(newCourse);
        ResponseEntity<Void> response = restTemplate.postForEntity(createURLWithPort("/students/Student1/courses"),
                request, Void.class);
        assertEquals(201, response.getStatusCodeValue());
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}