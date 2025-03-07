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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IntegrationTests {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRetrieveCoursesForStudent_Success() {
        String studentId = "Student1";
        List<Course> courses = Arrays.asList(
                new Course("Course1", "spring", "10 Steps", Arrays.asList("Learn Maven", "Import Project")),
                new Course("Course2", "spring MVC", "10 Steps", Arrays.asList("Learn Maven", "Import Project")));

        when(studentService.retreiveCourses(studentId)).thenReturn(courses);

        List<Course> result = studentController.retreiveCoursesForStudent(studentId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studentService, times(1)).retreiveCourses(studentId);
    }

    @Test
    public void testRetrieveDetailsForCourse_Success() {
        String studentId = "Student1";
        String courseId = "Course1";
        Course course = new Course(courseId, "spring", "10 Steps", Arrays.asList("Learn Maven", "Import Project"));

        when(studentService.retreiveCourse(studentId, courseId)).thenReturn(course);

        Course result = studentController.retreiveDetailsForCourse(studentId, courseId);

        assertNotNull(result);
        assertEquals(courseId, result.getId());
        verify(studentService, times(1)).retreiveCourse(studentId, courseId);
    }

    @Test
    public void testRegisterStudentForCourse_Success() {
        String studentId = "Student1";
        Course newCourse = new Course(null, "spring boot", "10 Steps", Arrays.asList("Learn Maven", "Import Project"));
        Course addedCourse = new Course("randomId", "spring boot", "10 Steps",
                Arrays.asList("Learn Maven", "Import Project"));

        when(studentService.addCourse(studentId, newCourse)).thenReturn(addedCourse);

        ResponseEntity<Void> response = studentController.registerStudentForCourse(studentId, newCourse);

        assertEquals(201, response.getStatusCodeValue());
        URI expectedLocation = ServletUriComponentsBuilder.fromCurrentRequest().path("/{studentId}")
                .buildAndExpand(addedCourse.getId()).toUri();
        assertEquals(expectedLocation, response.getHeaders().getLocation());
        verify(studentService, times(1)).addCourse(studentId, newCourse);
    }

    @Test
    public void testRegisterStudentForCourse_Failure() {
        String studentId = "Student1";
        Course newCourse = new Course(null, "spring boot", "10 Steps", Arrays.asList("Learn Maven", "Import Project"));

        when(studentService.addCourse(studentId, newCourse)).thenReturn(null);

        ResponseEntity<Void> response = studentController.registerStudentForCourse(studentId, newCourse);

        assertEquals(204, response.getStatusCodeValue());
        verify(studentService, times(1)).addCourse(studentId, newCourse);
    }
}