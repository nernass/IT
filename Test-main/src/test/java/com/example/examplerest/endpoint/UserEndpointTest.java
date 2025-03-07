//package com.example.examplerest.endpoint;
//
//import com.example.examplerest.model.User;
//import com.example.examplerest.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@ExtendWith(SpringExtension.class)
//@AutoConfigureMockMvc
//class UserEndpointTest {
//
//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private static final String BASE_URL = "http://localhost:8080";
//
//    @Test
//    public void register() throws Exception {
//        ObjectNode objectNode = new ObjectMapper().createObjectNode();
//        objectNode.put("name", "user");
//        objectNode.put("surname", "user");
//        objectNode.put("email", "user@gmail.com");
//        objectNode.put("password", "user");
//
//        mvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectNode.toString()))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    public void auth() throws Exception {
//        testAddUser();
//
//        ObjectNode objectNode = new ObjectMapper().createObjectNode();
//        objectNode.put("email", "user@mail.ru");
//        objectNode.put("password", "user");
//
//        mvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/user/auth")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectNode.toString()))
//                .andExpect(status().isOk());
//    }
//
//    private void testAddUser() {
//        userRepository.save(User.builder()
//                .name("user")
//                .surname("ser")
//                .email("user@mail.ru")
//                .password("user")
//                .build());
//    }
//}