package com.example.examplerest.integration;

import com.example.examplerest.dto.BookDto;
import com.example.examplerest.endpoint.BookEndpoint;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class BookIntegrationTest {

    @Autowired
    private BookEndpoint bookEndpoint;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @MockBean
    private RestTemplate restTemplate;

    private Author testAuthor;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        bookRepository.deleteAll();

        // Create test data
        testAuthor = Author.builder()
                .name("Test Author")
                .surname("Test Surname")
                .email("test@test.com")
                .gender(Gender.MALE)
                .build();

        testBook = Book.builder()
                .title("Test Book")
                .description("Test Description")
                .price(100.0)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        // Mock currency exchange API response
        HashMap<String, String> currencyResponse = new HashMap<>();
        currencyResponse.put("USD", "400");
        when(restTemplate.getForEntity(
                "https://cb.am/latest.json.php?currency=USD",
                HashMap.class)).thenReturn(ResponseEntity.ok(currencyResponse));
    }

    @Test
    void testCreateAndRetrieveBook() {
        // Create book
        ResponseEntity<?> createResponse = bookEndpoint.createBook(testBook);
        assertEquals(204, createResponse.getStatusCodeValue());

        // Verify book was saved
        List<Book> savedBooks = bookRepository.findAll();
        assertEquals(1, savedBooks.size());
        Book savedBook = savedBooks.get(0);
        assertEquals(testBook.getTitle(), savedBook.getTitle());
        assertEquals(testBook.getAuthor().getName(), savedBook.getAuthor().getName());

        // Retrieve book by ID
        ResponseEntity<Optional<Book>> getResponse = bookEndpoint.getBookById(savedBook.getId());
        assertTrue(getResponse.getBody().isPresent());
        assertEquals(testBook.getTitle(), getResponse.getBody().get().getTitle());
    }

    @Test
    void testGetAllBooksWithCurrencyConversion() {
        // Save multiple books
        Book book1 = testBook;
        Book book2 = Book.builder()
                .title("Another Book")
                .description("Another Description")
                .price(200.0)
                .language(BookLanguage.ARM)
                .author(testAuthor)
                .build();

        bookService.save(book1);
        bookService.save(book2);

        // Get all books
        ResponseEntity<List<BookDto>> response = bookEndpoint.getAllBooks();

        assertEquals(200, response.getStatusCodeValue());
        List<BookDto> books = response.getBody();
        assertNotNull(books);
        assertEquals(2, books.size());

        // Verify currency conversion (100 AMD / 400 USD = 0.25 USD)
        assertEquals(0.25, books.get(0).getPrice());
        assertEquals(0.5, books.get(1).getPrice());
    }

    @Test
    void testInvalidBookCreation() {
        // Test with invalid book (null title)
        Book invalidBook = Book.builder()
                .title(null)
                .description("Test Description")
                .price(100.0)
                .language(BookLanguage.EN)
                .author(testAuthor)
                .build();

        // Expect validation exception
        assertThrows(Exception.class, () -> {
            bookEndpoint.createBook(invalidBook);
        });

        // Verify no book was saved
        List<Book> savedBooks = bookRepository.findAll();
        assertEquals(0, savedBooks.size());
    }
}