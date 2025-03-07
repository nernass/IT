package com.example.examplerest.integration;

import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
    }

    @Test
    void shouldCreateAuthor() throws Exception {
        CreateAuthorDto authorDto = CreateAuthorDto.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        assertEquals(1, authorRepository.count());
    }

    @Test
    void shouldReturnAllAuthors() throws Exception {
        Author author1 = Author.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .gender(Gender.MALE)
                .build();

        Author author2 = Author.builder()
                .name("Jane")
                .surname("Doe")
                .email("jane@example.com")
                .gender(Gender.FEMALE)
                .build();

        authorRepository.save(author1);
        authorRepository.save(author2);

        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));
    }

    @Test
    void shouldRejectInvalidAuthor() throws Exception {
        CreateAuthorDto invalidAuthor = CreateAuthorDto.builder()
                .name("") // Invalid: empty name
                .surname("Doe")
                .email("") // Invalid: empty email
                .gender(null) // Invalid: null gender
                .build();

        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest());

        assertEquals(0, authorRepository.count());
    }
}