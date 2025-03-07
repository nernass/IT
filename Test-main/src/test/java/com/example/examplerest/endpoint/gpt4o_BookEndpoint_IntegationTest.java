package com.example.examplerest.integration;

import com.example.examplerest.endpoint.BookEndpoint;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookEndpoint.class)
public class BookIntegrationTest {

    @MockBean
    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @InjectMocks
    private BookEndpoint bookEndpoint;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookEndpoint).build();
    }

    @Test
    public void testGetAllBooks() throws Exception {
        Author author = new Author(1, "John", "Doe", "john.doe@example.com", null);
        Book book1 = new Book(1, "Book 1", "Description 1", 10.0, BookLanguage.EN, author);
        Book book2 = new Book(2, "Book 2", "Description 2", 20.0, BookLanguage.EN, author);

        when(bookService.findAll()).thenReturn(Arrays.asList(book1, book2));

        mockMvc.perform(get("/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[1].title").value("Book 2"));
    }

    @Test
    public void testGetBookById() throws Exception {
        Author author = new Author(1, "John", "Doe", "john.doe@example.com", null);
        Book book = new Book(1, "Book 1", "Description 1", 10.0, BookLanguage.EN, author);

        when(bookService.findById(1)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book 1"));
    }

    @Test
    public void testCreateBook() throws Exception {
        Author author = new Author(1, "John", "Doe", "john.doe@example.com", null);
        Book book = new Book(1, "Book 1", "Description 1", 10.0, BookLanguage.EN, author);

        when(bookService.save(any(Book.class))).thenReturn(book);

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"title\":\"Book 1\",\"description\":\"Description 1\",\"price\":10.0,\"language\":\"EN\",\"author\":{\"id\":1}}"))
                .andExpect(status().isNoContent());
    }
}