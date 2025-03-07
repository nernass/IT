package com.example.examplerest.integration;

import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
    }

    @Test
    void testCreateAuthor() throws Exception {
        CreateAuthorDto createAuthorDto = new CreateAuthorDto();
        createAuthorDto.setName("John");
        createAuthorDto.setSurname("Doe");
        createAuthorDto.setEmail("john.doe@example.com");
        createAuthorDto.setGender(Gender.MALE);

        MvcResult result = mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John\",\"surname\":\"Doe\",\"email\":\"john.doe@example.com\",\"gender\":\"MALE\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("John", "Doe", "john.doe@example.com", "MALE");

        Author author = authorRepository.findAll().get(0);
        assertThat(author.getName()).isEqualTo("John");
        assertThat(author.getSurname()).isEqualTo("Doe");
        assertThat(author.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(author.getGender()).isEqualTo(Gender.MALE);
    }

    @Test
    void testGetAllAuthors() throws Exception {
        Author author = new Author();
        author.setName("Jane");
        author.setSurname("Doe");
        author.setEmail("jane.doe@example.com");
        author.setGender(Gender.FEMALE);
        authorRepository.save(author);

        MvcResult result = mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("Jane", "Doe", "jane.doe@example.com", "FEMALE");
    }
}