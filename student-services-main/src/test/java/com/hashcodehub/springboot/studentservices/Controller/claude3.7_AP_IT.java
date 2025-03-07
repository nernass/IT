package com.hashcodehub.springboot.studentservices.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashcodehub.springboot.studentservices.Model.Course;
import com.hashcodehub.springboot.studentservices.Model.Student;
import com.hashcodehub.springboot.studentservices.Service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentServicesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRetrieveCoursesForStudent_Success() throws Exception {
        String studentId = "Student1";

        // Test retrieval of courses for a valid student
        mockMvc.perform(get("/students/{studentId}/courses", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", is("Course1")))
                .andExpect(jsonPath("$[0].name", is("spring")));

        // Verify the actual service returns the same data
        List<Course> courses = studentService.retreiveCourses(studentId);
        assertNotNull(courses);
        assertTrue(courses.size() > 0);
        assertEquals("Course1", courses.get(0).getId());
    }

    @Test
    public void testRetrieveCourseDetails_Success() throws Exception {
        String studentId = "Student1";
        String courseId = "Course1";

        // Test retrieval of a specific course for a valid student
        mockMvc.perform(get("/students/{studentId}/courses/{courseId}", studentId, courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(courseId)))
                .andExpect(jsonPath("$.name", is("spring")))
                .andExpect(jsonPath("$.description", is("10 Steps")))
                .andExpect(jsonPath("$.steps", hasSize(4)));

        // Verify with direct service call
        Course course = studentService.retreiveCourse(studentId, courseId);
        assertNotNull(course);
        assertEquals(courseId, course.getId());
        assertEquals("spring", course.getName());
    }

    @Test
    public void testRetrieveNonExistentStudent_Failure() throws Exception {
        String nonExistentStudentId = "NonExistentStudent";

        // Test behavior when student doesn't exist
        mockMvc.perform(get("/students/{studentId}/courses", nonExistentStudentId))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Expect empty response

        // Verify with direct service call
        List<Course> courses = studentService.retreiveCourses(nonExistentStudentId);
        assertNull(courses);
    }

    @Test
    public void testAddNewCourse_Success() throws Exception {
        String studentId = "Student2";
        Course newCourse = new Course(null, "Advanced Java", "Deep dive into Java",
                Arrays.asList("JVM Internals", "Memory Management", "Concurrency"));

        // Test adding a new course via API
        mockMvc.perform(post("/students/{studentId}/courses", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // Verify the course was actually added
        List<Course> updatedCourses = studentService.retreiveCourses(studentId);
        boolean courseFound = updatedCourses.stream()
                .anyMatch(c -> "Advanced Java".equals(c.getName()) &&
                        "Deep dive into Java".equals(c.getDescription()));
        assertTrue(courseFound);
    }

    @Test
    public void testAddCourseToNonExistentStudent_Failure() throws Exception {
        String nonExistentStudentId = "NonExistentStudent";
        Course newCourse = new Course(null, "Test Course", "Test Description",
                Arrays.asList("Step 1", "Step 2"));

        // Test adding a course to a non-existent student
        mockMvc.perform(post("/students/{studentId}/courses", nonExistentStudentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testRetrieveCourseNotFoundForStudent_Failure() throws Exception {
        String studentId = "Student1";
        String nonExistentCourseId = "NonExistentCourse";

        // Test behavior when course doesn't exist
        mockMvc.perform(get("/students/{studentId}/courses/{courseId}", studentId, nonExistentCourseId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        // Verify with direct service call
        Course course = studentService.retreiveCourse(studentId, nonExistentCourseId);
        assertNull(course);
    }

    @Test
    public void testEndToEndWorkflow() throws Exception {
        // 1. Get a student
        String studentId = "Student1";
        Student student = studentService.retreiveStudent(studentId);
        assertNotNull(student);

        // 2. Get their courses
        List<Course> initialCourses = studentService.retreiveCourses(studentId);
        int initialCourseCount = initialCourses.size();

        // 3. Add a new course
        Course newCourse = new Course(null, "Integration Testing", "Testing whole systems",
                Arrays.asList("Plan", "Implement", "Execute", "Report"));

        mockMvc.perform(post("/students/{studentId}/courses", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCourse)))
                .andExpect(status().isCreated());

        // 4. Verify course was added
        List<Course> updatedCourses = studentService.retreiveCourses(studentId);
        assertEquals(initialCourseCount + 1, updatedCourses.size());

        // 5. Find the newly added course
        Course addedCourse = updatedCourses.stream()
                .filter(c -> "Integration Testing".equals(c.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(addedCourse);
        assertNotNull(addedCourse.getId());

        // 6. Fetch the specific course using the API
        mockMvc.perform(get("/students/{studentId}/courses/{courseId}", studentId, addedCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(addedCourse.getId())))
                .andExpect(jsonPath("$.name", is("Integration Testing")))
                .andExpect(jsonPath("$.steps", hasSize(4)));
    }
}