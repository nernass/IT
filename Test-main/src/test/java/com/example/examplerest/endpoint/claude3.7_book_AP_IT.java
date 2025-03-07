package com.example.examplerest.integration;

import com.example.examplerest.dto.BookDto;
import com.example.examplerest.mapper.BookMapper;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private MockRestServiceServer mockServer;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        bookRepository.deleteAll();

        // Set up mock server for external currency API
        mockServer = MockRestServiceServer.createServer(restTemplate);

        // Create test data
        testAuthor = Author.builder()
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .gender(Gender.MALE)
                .build();

        // Persist the author first
        entityManager.persist(testAuthor);
        entityManager.flush();

        testBook = Book.builder()
                .title("Test Book")
                .description("Test Description")
                .price(500.0)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();
    }

    @Test
    public void testGetAllBooks_Success() throws Exception {
        // Setup mock response from currency API
        String currencyResponse = "{\"USD\":\"400\"}";
        mockServer.expect(requestTo("https://cb.am/latest.json.php?currency=USD"))
                .andRespond(withSuccess(currencyResponse, MediaType.APPLICATION_JSON));

        // Save test data to repository
        bookRepository.save(testBook);

        // Perform GET request and verify response
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Book")))
                .andExpect(jsonPath("$[0].price", closeTo(1.25, 0.01))); // 500/400 = 1.25
    }

    @Test
    public void testGetBookById_Success() throws Exception {
        // Save test data to repository
        Book savedBook = bookRepository.save(testBook);

        // Perform GET request and verify response
        mockMvc.perform(get("/books/{id}", savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present", is(true)));
    }

    @Test
    public void testGetBookById_NotFound() throws Exception {
        // Perform GET request with non-existent ID
        mockMvc.perform(get("/books/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateBook_Success() throws Exception {
        // Prepare book data
        Book newBook = Book.builder()
                .title("New Book")
                .description("New Description")
                .price(300.0)
                .language(BookLanguage.ARM)
                .author(testAuthor)
                .build();

        // Perform POST request and verify response
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isNoContent());

        // Verify book was saved to repository
        Assertions.assertEquals(1, bookRepository.count());
    }

    @Test
    public void testCreateBook_ValidationFailure() throws Exception {
        // Create invalid book (missing required fields)
        Book invalidBook = Book.builder()
                .title("") // Empty title - should fail validation
                .price(300.0)
                .language(BookLanguage.ARM)
                .author(testAuthor)
                .build();

        // Perform POST request and verify validation error
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        // Verify no book was saved
        Assertions.assertEquals(0, bookRepository.count());
    }

    @Test
    public void testEndToEndBookWorkflow() throws Exception {
        // Setup mock response from currency API
        String currencyResponse = "{\"USD\":\"400\"}";
        mockServer.expect(requestTo("https://cb.am/latest.json.php?currency=USD"))
                .andRespond(withSuccess(currencyResponse, MediaType.APPLICATION_JSON));

        // 1. Create a new book
        Book newBook = Book.builder()
                .title("Integration Test Book")
                .description("Testing full workflow")
                .price(800.0)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        String bookJson = objectMapper.writeValueAsString(newBook);

        // 2. POST the book
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookJson))
                .andExpect(status().isNoContent());

        // 3. Get all books and verify the new book is included with correct USD price
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Integration Test Book")))
                .andExpect(jsonPath("$[0].price", closeTo(2.0, 0.01))); // 800/400 = 2.0

        // 4. Get the book by ID
        Book savedBook = bookRepository.findAll().get(0);
        mockMvc.perform(get("/books/{id}", savedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present", is(true)));
    }

    @Test
    public void testCurrencyConversionFailure() throws Exception {
        // Setup mock response from currency API with invalid data
        String invalidCurrencyResponse = "{\"USD\":\"invalid\"}";
        mockServer.expect(requestTo("https://cb.am/latest.json.php?currency=USD"))
                .andRespond(withSuccess(invalidCurrencyResponse, MediaType.APPLICATION_JSON));

        // Save test data to repository
        bookRepository.save(testBook);

        // Perform GET request and verify response - should still return books but with
        // original price
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Test Book")))
                .andExpect(jsonPath("$[0].price", is(500.0))); // Original price should remain
    }

    @Test
    public void testBookWithNullAuthor() throws Exception {
        // Create book with null author (violates @NotNull constraint)
        Book invalidBook = Book.builder()
                .title("Book Without Author")
                .description("This book has no author")
                .price(150.0)
                .language(BookLanguage.EN)
                .author(null)
                .build();

        // Perform POST request and verify validation error
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBookPriceValidation() throws Exception {
        // Create book with price outside allowed range
        Book invalidBook = Book.builder()
                .title("Expensive Book")
                .description("This book is too expensive")
                .price(1500.0) // Over 1000 - should fail validation
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        // Perform POST request and verify validation error
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());

        // Try with negative price
        invalidBook.setPrice(-10.0);
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }
}