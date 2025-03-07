package com.example.examplerest.endpoint;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
//@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
//@ActiveProfiles("test")
public class AuthorEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AuthorRepository authorRepository;

    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    public void beforeAll() {
        authorRepository.deleteAll();
    }

    @Test
//    @WithUserDetails("poxos@gmail.com")

//    @WithMockUser(username = "poxos", authorities = "ADMIN", roles = "ADMIN")
//    @Sql(scripts = )
    public void testGetAllAuthorsEndpoint() throws Exception {
        testAddAuthors();

        mvc.perform(get(BASE_URL + "/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void createAuthor() throws Exception {
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("name", "poxos");
        objectNode.put("surname", "poxos");
        objectNode.put("email", "poxos@gmail.com");
//        objectNode.put("password", "poxos");
        objectNode.put("gender", "MALE");

        //save author
        mvc.perform(post(BASE_URL + "/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectNode.toString()))
                .andExpect(status().isOk());
//        //get all authors
//        mvc.perform(get(BASE_URL + "/authors")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)));
    }

    private void testAddAuthors() {
        authorRepository.save(Author.builder()
                .email("test1@mail.com")
                .gender(Gender.MALE)
                .surname("poxosyan")
                .name("poxos")
                .build());

        authorRepository.save(Author.builder()
                .email("test2@mail.com")
                .gender(Gender.MALE)
                .surname("petrosyan")
                .name("petros")
                .build());
    }
}