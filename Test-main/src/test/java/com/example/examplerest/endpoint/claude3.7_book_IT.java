package com.example.examplerest.integration;

import com.example.examplerest.dto.BookDto;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @MockBean
    private RestTemplate restTemplate;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        bookRepository.deleteAll();

        // Setup test data
        testAuthor = Author.builder()
                .name("Test Author")
                .surname("Surname")
                .email("test@example.com")
                .gender(Gender.MALE)
                .build();

        testBook = Book.builder()
                .title("Test Book")
                .description("Test Description")
                .price(29.99)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        // Mock currency exchange API
        HashMap<String, String> currencyMap = new HashMap<>();
        currencyMap.put("USD", "400");
        when(restTemplate.getForEntity(anyString(), eq(HashMap.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(currencyMap));
    }

    @Test
    void testCreateAndGetBook() throws Exception {
        // Create book
        String authorJson = objectMapper.writeValueAsString(testAuthor);
        MvcResult authorResult = mockMvc.perform(post("/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authorJson))
                .andExpect(status().isNoContent())
                .andReturn();

        // Get the saved author from the database
        List<Author> authors = bookService.findAllAuthors();
        assertFalse(authors.isEmpty());
        Author savedAuthor = authors.get(0);
        testBook.setAuthor(savedAuthor);

        // Create book with the saved author
        String bookJson = objectMapper.writeValueAsString(testBook);
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isNoContent());

        // Get all books
        MvcResult result = mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andReturn();

        // Verify book details
        mockMvc.perform(get("/books/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(testBook.getTitle())))
                .andExpect(jsonPath("$.description", is(testBook.getDescription())));
    }

    @Test
    void testGetBookWithNonexistentId() throws Exception {
        // Attempt to get book with non-existent ID
        mockMvc.perform(get("/books/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBookWithInvalidData() throws Exception {
        // Test with invalid book (no title)
        Book invalidBook = Book.builder()
                .title("")
                .description("Test Description")
                .price(29.99)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        String invalidBookJson = objectMapper.writeValueAsString(invalidBook);
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBookJson))
                .andExpect(status().isBadRequest());

        // Test with invalid price range
        Book invalidPriceBook = Book.builder()
                .title("Test Book")
                .description("Test Description")
                .price(1500) // Over max range
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        String invalidPriceBookJson = objectMapper.writeValueAsString(invalidPriceBook);
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPriceBookJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllBooksWithCurrencyConversion() throws Exception {
        // Create author and book
        Author author = Author.builder()
                .name("Currency Test")
                .surname("Author")
                .email("currency@test.com")
                .gender(Gender.FEMALE)
                .build();

        // Save the author to DB first
        bookService.saveAuthor(author);

        // Get the saved author
        List<Author> authors = bookService.findAllAuthors();
        assertFalse(authors.isEmpty());

        // Create book with the saved author
        Book book = Book.builder()
                .title("Currency Test Book")
                .description("Testing currency conversion")
                .price(400.0) // This should become 1.0 USD with mock rate of 400
                .language(BookLanguage.EN)
                .author(authors.get(0))
                .build();

        bookService.save(book);

        // Get all books and verify currency conversion
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price", closeTo(1.0, 0.01)));

        // Verify the RestTemplate was called
        verify(restTemplate).getForEntity(eq("https://cb.am/latest.json.php?currency=USD"), eq(HashMap.class));
    }
}