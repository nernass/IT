package com.hashcodehub.springboot.studentservices.integration;

import com.hashcodehub.springboot.studentservices.Controller.StudentController;
import com.hashcodehub.springboot.studentservices.Model.Course;
import com.hashcodehub.springboot.studentservices.Service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course("testCourse1", "Test Course", "Test Description",
                Arrays.asList("Step 1", "Step 2", "Step 3"));
    }

    @Test
    void whenGetCourses_thenReturnCoursesList() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/students/Student1/courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id", notNullValue()));
    }

    @Test
    void whenGetSpecificCourse_thenReturnCourse() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/students/Student1/courses/Course1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("Course1")))
                .andExpect(jsonPath("$.name", is("spring")));
    }

    @Test
    void whenAddNewCourse_thenCreateCourse() throws Exception {
        String courseJson = objectMapper.writeValueAsString(testCourse);

        mockMvc.perform(MockMvcRequestBuilders.post("/students/Student1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
}

@Test
    void whenGetNonExistentStudent_thenReturn