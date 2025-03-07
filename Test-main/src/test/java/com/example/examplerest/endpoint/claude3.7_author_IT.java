package com.example.examplerest.endpoint;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import com.example.examplerest.security.CurrentUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthorEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Author testAuthor;

    @BeforeEach
    public void setup() {
        authorRepository.deleteAll();

        testAuthor = Author.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .gender(Gender.MALE)
                .build();

        authorRepository.save(testAuthor);
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testGetAllAuthors() throws Exception {
        // Perform GET request
        MvcResult mvcResult = mockMvc.perform(get("/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Parse response
        String responseContent = mvcResult.getResponse().getContentAsString();
        List<AuthorResponseDto> authors = objectMapper.readValue(
                responseContent,
                objectMapper.getTypeFactory().constructCollectionType(List.class, AuthorResponseDto.class));

        // Assertions
        assertNotNull(authors);
        assertFalse(authors.isEmpty());
        assertEquals(testAuthor.getName(), authors.get(0).getName());
        assertEquals(testAuthor.getSurname(), authors.get(0).getSurname());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testCreateAuthor() throws Exception {
        // Prepare request
        CreateAuthorDto createAuthorDto = new CreateAuthorDto();
        createAuthorDto.setName("Jane");
        createAuthorDto.setSurname("Smith");
        createAuthorDto.setEmail("jane.smith@example.com");
        createAuthorDto.setGender(Gender.FEMALE);

        String requestJson = objectMapper.writeValueAsString(createAuthorDto);

        // Perform POST request
        MvcResult mvcResult = mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Parse response
        String responseContent = mvcResult.getResponse().getContentAsString();
        Author createdAuthor = objectMapper.readValue(responseContent, Author.class);

        // Assertions
        assertNotNull(createdAuthor);
        assertNotNull(createdAuthor.getId());
        assertEquals(createAuthorDto.getName(), createdAuthor.getName());
        assertEquals(createAuthorDto.getSurname(), createdAuthor.getSurname());
        assertEquals(createAuthorDto.getEmail(), createdAuthor.getEmail());
        assertEquals(createAuthorDto.getGender(), createdAuthor.getGender());

        // Verify author was saved to repository
        assertTrue(authorRepository.findById(createdAuthor.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testCreateAuthorWithInvalidData() throws Exception {
        // Prepare request with invalid data (missing required fields)
        CreateAuthorDto invalidAuthorDto = new CreateAuthorDto();
        invalidAuthorDto.setSurname("Smith");
        // Missing name, email, and gender

        String requestJson = objectMapper.writeValueAsString(invalidAuthorDto);

        // Perform POST request - should fail validation
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());

        // Verify no new author was added
        assertEquals(1, authorRepository.count());
    }

    @Test
    @WithMockUser(username = "user", roles = { "USER" })
    public void testRepositoryAndModelInteraction() {
        // Create a new author
        Author newAuthor = Author.builder()
                .name("Robert")
                .surname("Johnson")
                .email("robert.johnson@example.com")
                .gender(Gender.MALE)
                .build();

        // Save to repository
        Author savedAuthor = authorRepository.save(newAuthor);

        // Verify saved correctly
        assertNotNull(savedAuthor.getId());

        // Retrieve and verify
        Author retrievedAuthor = authorRepository.findById(savedAuthor.getId()).orElse(null);
        assertNotNull(retrievedAuthor);
        assertEquals(newAuthor.getName(), retrievedAuthor.getName());
        assertEquals(newAuthor.getEmail(), retrievedAuthor.getEmail());

        // Delete
        authorRepository.delete(retrievedAuthor);
        assertFalse(authorRepository.findById(retrievedAuthor.getId()).isPresent());
    }
}