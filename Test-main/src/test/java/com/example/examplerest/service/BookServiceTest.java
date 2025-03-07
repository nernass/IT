package com.example.examplerest.service;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class BookServiceTest {

    @MockBean
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @BeforeEach
    public void beforeAll() {
        bookRepository.deleteAll();
    }

    @Test
    void save() {
        Book book = Book.builder()
                .id(1)
                .author(new Author())
                .description("asaa")
                .price(33)
                .title("title")
                .build();
        when(bookRepository.save(any())).thenReturn(book);

        bookService.save(Book.builder()
                .author(new Author())
                .description("asaa")
                .price(33)
                .title("title")
                .build());

        verify(bookRepository, times(1)).save(any());
    }

    @Test
    public void findById() {
        testAddBooks();

        int id = 1;

        Optional<Book> actual = bookService.findById(id);
        Optional<Book> expected = bookRepository.findById(id);

        assertEquals(expected, actual);
    }

    @Test
    public void findAll() {
        testAddBooks();

        List<Book> actual = bookService.findAll();
        List<Book> expected = bookRepository.findAll();

        assertEquals(expected, actual);
    }

    private void testAddBooks() {
        bookRepository.save(Book.builder()
                .title("book1")
                .description("desc1")
                .price(456.456)
                .language(BookLanguage.EN)
                .build());
        bookRepository.save(Book.builder()
                .title("book2")
                .description("desc2")
                .price(456.4165)
                .language(BookLanguage.ARM)
                .build());

    }
}