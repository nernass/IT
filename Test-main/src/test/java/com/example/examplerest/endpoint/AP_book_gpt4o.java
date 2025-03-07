package com.example.examplerest.integration;

import com.example.examplerest.dto.BookDto;
import com.example.examplerest.endpoint.BookEndpoint;
import com.example.examplerest.exception.EntityNotFoundException;
import com.example.examplerest.mapper.BookMapper;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BookEndpointIntegrationTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private BookService bookService;

    @MockBean
    private BookMapper bookMapper;

    @InjectMocks
    private BookEndpoint bookEndpoint;

    private Book book;
    private Author author;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        author = Author.builder()
                .id(1)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .gender(Gender.MALE)
                .build();
        book = Book.builder()
                .id(1)
                .title("Test Book")
                .description("Test Description")
                .price(100)
                .language(BookLanguage.EN)
                .author(author)
                .build();
    }

    @Test
    public void testGetAllBooks_Success() {
        List<Book> books = Arrays.asList(book);
        when(bookService.findAll()).thenReturn(books);
        HashMap<String, String> currencyMap = new HashMap<>();
        currencyMap.put("USD", "1.0");
        when(restTemplate.getForEntity(anyString(), eq(HashMap.class))).thenReturn(ResponseEntity.ok(currencyMap));
        when(bookMapper.map(books)).thenReturn(Arrays.asList(new BookDto()));

        ResponseEntity<List<BookDto>> response = bookEndpoint.getAllBooks();

        assertEquals(200, response.getStatusCodeValue());
        verify(bookService, times(1)).findAll();
        verify(restTemplate, times(1)).getForEntity(anyString(), eq(HashMap.class));
        verify(bookMapper, times(1)).map(books);
    }

    @Test
    public void testGetBookById_Success() throws EntityNotFoundException {
        when(bookService.findById(anyInt())).thenReturn(Optional.of(book));

        ResponseEntity<Optional<Book>> response = bookEndpoint.getBookById(1);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(book, response.getBody().get());
        verify(bookService, times(1)).findById(anyInt());
    }

    @Test
    public void testCreateBook_Success() {
        doNothing().when(bookService).save(any(Book.class));

        ResponseEntity<?> response = bookEndpoint.createBook(book);

        assertEquals(204, response.getStatusCodeValue());
        verify(bookService, times(1)).save(any(Book.class));
    }
}