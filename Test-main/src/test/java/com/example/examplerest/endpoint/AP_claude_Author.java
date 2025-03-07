package com.example.examplerest.integration;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    public void testCreateAndRetrieveAuthor() throws Exception {
        // Create author DTO
        CreateAuthorDto createAuthorDto = CreateAuthorDto.builder()
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .gender(Gender.MALE)
                .build();

        // Create author through API
        MvcResult createResult = mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andReturn();

        Author createdAuthor = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                Author.class);

        // Verify author was saved in repository
        Author savedAuthor = authorRepository.findById(createdAuthor.getId())
                .orElseThrow();
        assertEquals(createAuthorDto.getName(), savedAuthor.getName());
        assertEquals(createAuthorDto.getEmail(), savedAuthor.getEmail());

        // Retrieve all authors through API
        MvcResult getResult = mockMvc.perform(get("/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<AuthorResponseDto> authors = objectMapper.readValue(
                getResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, AuthorResponseDto.class));

        // Verify retrieved author matches created author
        assertTrue(authors.stream()
                .anyMatch(author -> author.getName().equals(createAuthorDto.getName()) &&
                        author.getEmail().equals(createAuthorDto.getEmail())));
    }

    @Test
    public void testCreateAuthorWithInvalidData() throws Exception {
        // Test with empty name
        CreateAuthorDto invalidAuthor = CreateAuthorDto.builder()
                .name("")
                .surname("Doe")
                .email("john@example.com")
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest());

        // Verify no author was saved
        assertEquals(authorRepository.count(), 0);
    }

    @Test
    public void testCreateDuplicateAuthor() throws Exception {
        CreateAuthorDto authorDto = CreateAuthorDto.builder()
                .name("Jane")
                .surname("Doe")
                .email("jane@example.com")
                .gender(Gender.FEMALE)
                .build();

        // Create first author
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isOk());

        // Try to create another author with same data
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorDto)))
                .andExpect(status().isOk());

        // Verify two authors were saved (since there's no unique constraint)
        assertEquals(2, authorRepository.count());
    }
}