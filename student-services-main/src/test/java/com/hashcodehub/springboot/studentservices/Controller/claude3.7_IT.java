package com.hashcodehub.springboot.studentservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashcodehub.springboot.studentservices.Model.Course;
import com.hashcodehub.springboot.studentservices.Model.Student;
import com.hashcodehub.springboot.studentservices.Service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STUDENT_ID = "Student1";
    private static final String COURSE_ID = "Course1";

    @BeforeEach
    public void setup() {
        // No additional setup needed as we're using the static data from StudentService
    }

    @Test
    public void testRetrieveCoursesForStudent() throws Exception {
        // Verify that courses can be retrieved for an existing student
        mockMvc.perform(get("/students/{studentId}/courses", STUDENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(COURSE_ID));
    }

    @Test
    public void testRetrieveCoursesForNonExistentStudent() throws Exception {
        // Verify behavior when student does not exist
        mockMvc.perform(get("/students/{studentId}/courses", "NonExistentStudent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void testRetrieveDetailsForCourse() throws Exception {
        // Verify course details can be retrieved
        mockMvc.perform(get("/students/{studentId}/courses/{courseId}", STUDENT_ID, COURSE_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(COURSE_ID))
                .andExpect(jsonPath("$.name").value("spring"));
    }

    @Test
    public void testRetrieveDetailsForNonExistentCourse() throws Exception {
        // Verify behavior when course does not exist
        mockMvc.perform(get("/students/{studentId}/courses/{courseId}", STUDENT_ID, "NonExistentCourse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void testRegisterStudentForCourse() throws Exception {
        // Create a new course
        Course newCourse = new Course(null, "Spring Security", "Learn Spring Security",
                Arrays.asList("Authentication", "Authorization", "OAuth2"));

        String courseJson = objectMapper.writeValueAsString(newCourse);

        // Register the course for an existing student
        MvcResult result = mockMvc.perform(post("/students/{studentId}/courses", STUDENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();

        // Extract course ID from location header
        String location = result.getResponse().getHeader("Location");
        String courseId = location.substring(location.lastIndexOf("/") + 1);

        // Verify the course was added to the student
        List<Course> courses = studentService.retreiveCourses(STUDENT_ID);
        boolean courseFound = courses.stream()
                .anyMatch(c -> c.getName().equals("Spring Security") && c.getId().equals(courseId));

        assertTrue(courseFound, "Added course should be found in student's course list");
    }

    @Test
    public void testRegisterCourseForNonExistentStudent() throws Exception {
        // Create a new course
        Course newCourse = new Course(null, "Spring Security", "Learn Spring Security",
                Arrays.asList("Authentication", "Authorization", "OAuth2"));

        String courseJson = objectMapper.writeValueAsString(newCourse);

        // Try to register the course for a non-existent student
        mockMvc.perform(post("/students/{studentId}/courses", "NonExistentStudent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testServiceStudentRetrieval() {
        // Test the service layer directly
        Student student = studentService.retreiveStudent(STUDENT_ID);
        assertNotNull(student, "Student should not be null");
        assertEquals("Ranga Karanam", student.getName());

        // Verify non-existent student returns null
        student = studentService.retreiveStudent("NonExistentStudent");
        assertNull(student, "Non-existent student should return null");
    }

    @Test
    public void testServiceCourseRetrieval() {
        // Test course retrieval from service layer
        List<Course> courses = studentService.retreiveCourses(STUDENT_ID);
        assertNotNull(courses, "Courses should not be null");
        assertFalse(courses.isEmpty(), "Courses should not be empty");

        // Test specific course retrieval
        Course course = studentService.retreiveCourse(STUDENT_ID, COURSE_ID);
        assertNotNull(course, "Course should not be null");
        assertEquals("spring", course.getName());
    }
}