package com.example.examplerest.integration;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.endpoint.AuthorEndpoint;
import com.example.examplerest.mapper.AuthorMapper;
import com.example.examplerest.model.Author;
import com.example.examplerest.repository.AuthorRepository;
import com.example.examplerest.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorEndpoint.class)
public class AuthorEndpointIntegrationTest {

    @MockBean
    private AuthorService authorService;

    @MockBean
    private AuthorMapper authorMapper;

    @MockBean
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorEndpoint authorEndpoint;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authorEndpoint).build();
    }

    @Test
    public void testGetAllAuthors_Success() throws Exception {
        Author author = new Author(1, "John", "Doe", "john.doe@example.com", Gender.MALE);
        AuthorResponseDto authorResponseDto = new AuthorResponseDto(1, "John", "Doe", "john.doe@example.com",
                Gender.MALE);
        when(authorService.findAll()).thenReturn(Collections.singletonList(author));
        when(authorMapper.map(any(List.class))).thenReturn(Collections.singletonList(authorResponseDto));

        mockMvc.perform(get("/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    @Test
    public void testCreateAuthor_Success() throws Exception {
        CreateAuthorDto createAuthorDto = new CreateAuthorDto("John", "Doe", "john.doe@example.com", Gender.MALE);
        Author author = new Author(1, "John", "Doe", "john.doe@example.com", Gender.MALE);
        when(authorMapper.map(any(CreateAuthorDto.class))).thenReturn(author);
        when(authorService.save(any(Author.class))).thenReturn(author);

        mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"name\":\"John\",\"surname\":\"Doe\",\"email\":\"john.doe@example.com\",\"gender\":\"MALE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    // Additional test cases for failure and edge cases can be added here
}