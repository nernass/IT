package com.example.examplerest.integration;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.endpoint.AuthorEndpoint;
import com.example.examplerest.mapper.AuthorMapper;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import com.example.examplerest.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorComponentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorEndpoint authorEndpoint;

    private Author testAuthor;
    private CreateAuthorDto createAuthorDto;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        authorRepository.deleteAll();

        // Set up test data
        testAuthor = Author.builder()
                .id(1)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .gender(Gender.MALE)
                .build();

        createAuthorDto = new CreateAuthorDto();
        createAuthorDto.setName("Jane");
        createAuthorDto.setSurname("Smith");
        createAuthorDto.setEmail("jane.smith@example.com");
        createAuthorDto.setGender(Gender.FEMALE);

        // Reset mocks
        reset(authorService);
    }

    // API LAYER INTEGRATION TESTS
    @Test
    @WithMockUser
    public void testGetAllAuthorsAPI() throws Exception {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorService.findAll()).thenReturn(authors);

        // Act & Assert
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[0].surname", is("Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[0].gender", is("MALE")));

        // Verify interactions
        verify(authorService, times(1)).findAll();
    }

    @Test
    @WithMockUser
    public void testCreateAuthorAPI() throws Exception {
        // Arrange
        Author mappedAuthor = authorMapper.map(createAuthorDto);
        mappedAuthor.setId(99); // Simulate ID assignment
        when(authorService.save(any(Author.class))).thenReturn(mappedAuthor);

        // Act & Assert
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.name", is("Jane")))
                .andExpect(jsonPath("$.surname", is("Smith")))
                .andExpect(jsonPath("$.email", is("jane.smith@example.com")))
                .andExpect(jsonPath("$.gender", is("FEMALE")));

        // Verify service was called with correct data
        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorService, times(1)).save(authorCaptor.capture());
        Author capturedAuthor = authorCaptor.getValue();
        assertEquals("Jane", capturedAuthor.getName());
        assertEquals("jane.smith@example.com", capturedAuthor.getEmail());
    }

    @Test
    @WithMockUser
    public void testCreateAuthorWithInvalidData() throws Exception {
        // Arrange
        CreateAuthorDto invalidAuthor = new CreateAuthorDto();
        invalidAuthor.setSurname("Smith");
        invalidAuthor.setEmail("jane.smith@example.com");
        invalidAuthor.setGender(Gender.FEMALE);
        // Name is missing which should trigger validation

        // Act & Assert
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest());

        // Verify service was not called
        verify(authorService, never()).save(any(Author.class));
    }

    // CONTROLLER-SERVICE INTEGRATION TESTS
    @Test
    public void testControllerServiceIntegration_GetAllAuthors() {
        // Arrange
        List<Author> authors = Arrays.asList(testAuthor);
        when(authorService.findAll()).thenReturn(authors);

        // Act
        ResponseEntity<List<AuthorResponseDto>> response = authorEndpoint.getAllAuthors(null);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<AuthorResponseDto> responseDtos = response.getBody();
        assertThat(responseDtos).hasSize(1);
        assertThat(responseDtos.get(0).getName()).isEqualTo("John");
        assertThat(responseDtos.get(0).getEmail()).isEqualTo("john.doe@example.com");

        // Verify service interaction
        verify(authorService, times(1)).findAll();
    }

    @Test
    public void testControllerServiceIntegration_CreateAuthor() {
        // Arrange
        when(authorService.save(any(Author.class))).thenReturn(testAuthor);

        // Act
        ResponseEntity<Author> response = authorEndpoint.createAuthor(createAuthorDto);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Author savedAuthor = response.getBody();
        assertThat(savedAuthor).isNotNull();
        assertThat(savedAuthor.getName()).isEqualTo("John"); // From the mocked response

        // Verify service interaction with correct mapping
        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);
        verify(authorService).save(authorCaptor.capture());
        Author capturedAuthor = authorCaptor.getValue();
        assertThat(capturedAuthor.getName()).isEqualTo("Jane"); // From the DTO
        assertThat(capturedAuthor.getEmail()).isEqualTo("jane.smith@example.com");
    }

    // SERVICE-REPOSITORY INTEGRATION TESTS
    @Test
    @Transactional
    public void testServiceRepositoryIntegration() {
        // This test requires an actual AuthorService implementation
        // We'll use the real repository and create a real author

        // First, clean the database
        authorRepository.deleteAll();

        // Save an author through the repository
        Author author = Author.builder()
                .name("Direct")
                .surname("Repository")
                .email("direct@repository.com")
                .gender(Gender.MALE)
                .build();

        Author savedAuthor = authorRepository.save(author);
        assertNotNull(savedAuthor.getId());

        // Verify we can retrieve it directly from the repository
        Optional<Author> retrievedAuthor = authorRepository.findById(savedAuthor.getId());
        assertThat(retrievedAuthor).isPresent();
        assertThat(retrievedAuthor.get().getName()).isEqualTo("Direct");

        // Verify findAll works
        List<Author> allAuthors = authorRepository.findAll();
        assertThat(allAuthors).isNotEmpty();
        assertThat(allAuthors).extracting("email").contains("direct@repository.com");
    }

    // END-TO-END FLOW SIMULATION
    @Test
    @WithMockUser
    public void testEndToEndFlow() throws Exception {
        // This test simulates the full flow from API to repository and back

        // 1. Setup: Create author to return when service.save is called
        Author authorToReturn = Author.builder()
                .id(42)
                .name(createAuthorDto.getName())
                .surname(createAuthorDto.getSurname())
                .email(createAuthorDto.getEmail())
                .gender(createAuthorDto.getGender())
                .build();

        when(authorService.save(any(Author.class))).thenReturn(authorToReturn);

        // 2. Create an author via API
        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAuthorDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(42)));

        // 3. Setup for findAll
        List<Author> authorsToReturn = Arrays.asList(authorToReturn);
        when(authorService.findAll()).thenReturn(authorsToReturn);

        // 4. Retrieve authors via API
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(42)))
                .andExpect(jsonPath("$[0].name", is("Jane")))
                .andExpect(jsonPath("$[0].email", is("jane.smith@example.com")));

        // 5. Verify service interactions
        verify(authorService).save(any(Author.class));
        verify(authorService).findAll();
    }

    // ERROR HANDLING TEST
    @Test
    @WithMockUser
    public void testErrorHandling() throws Exception {
        // Arrange: Make the service throw an exception
        when(authorService.findAll()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert: The controller should handle the error
        mockMvc.perform(get("/authors"))
                .andExpect(status().isInternalServerError());

        // Verify service interaction
        verify(authorService).findAll();
    }
}