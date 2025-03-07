package com.example.examplerest.integration;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testAuthor = Author.builder()
                .name("Test Author")
                .surname("Test Surname")
                .email("test@test.com")
                .gender(Gender.MALE)
                .build();

        testBook = Book.builder()
                .title("Test Book")
                .description("Test Description")
                .price(29.99)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();
    }

    @Test
    void shouldCreateAndRetrieveBook() throws Exception {
        // Create book
        String bookJson = objectMapper.writeValueAsString(testBook);

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isNoContent());

        // Verify book exists in repository
        assertEquals(1, bookRepository.count());

        // Retrieve all books
        mockMvc.perform(MockMvcRequestBuilders.get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value(testBook.getTitle()));
    }

    @Test
    void shouldReturnBookById() throws Exception {
        // Save book directly via service
        Book savedBook = bookService.save(testBook);

        // Retrieve book by ID through API
        mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present").value(true));
    }

    @Test
    void shouldReturnNotFoundForNonExistentBook() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/books/{id}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present").value(false));
    }

    @Test
    void shouldValidateBookFields() throws Exception {
        testBook.setTitle(""); // Invalid title
        String bookJson = objectMapper.writeValueAsString(testBook);

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidPriceRange() throws Exception {
        testBook.setPrice(1001); // Outside valid range
        String bookJson = objectMapper.writeValueAsString(testBook);

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateAuthorFields() throws Exception {
        testAuthor.setEmail(null); // Invalid email
        testBook.setAuthor(testAuthor);
        String bookJson = objectMapper.writeValueAsString(testBook);

        mockMvc.perform(MockMvcRequestBuilders.post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isBadRequest());
    }
}