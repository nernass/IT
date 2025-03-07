package com.devtalkers.restapitest.integration;

import com.devtalkers.restapitest.controllers.UserController;
import com.devtalkers.restapitest.entity.User;
import com.devtalkers.restapitest.repository.UserRepository;
import com.devtalkers.restapitest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        // given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setGender("Male");
        user.setPhone("1234567890");

        when(userService.saveUser(any(User.class))).thenReturn(user);

        // when
        ResultActions response = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.gender", is(user.getGender())))
                .andExpect(jsonPath("$.phone", is(user.getPhone())));
    }

    @Test
    void shouldRetrieveUserById() throws Exception {
        // given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setGender("Male");
        user.setPhone("1234567890");
        userRepository.save(user);

        when(userService.getUserById(user.getId())).thenReturn(user);

        // when
        ResultActions response = mockMvc.perform(get("/user/{id}", user.getId()));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.gender", is(user.getGender())))
                .andExpect(jsonPath("$.phone", is(user.getPhone())));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        // given
        int nonExistentId = 999;

        when(userService.getUserById(nonExistentId)).thenReturn(null);

        // when
        ResultActions response = mockMvc.perform(get("/user/{id}", nonExistentId));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound());
    }
}