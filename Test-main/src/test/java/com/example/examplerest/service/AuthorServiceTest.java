package com.example.examplerest.service;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.Gender;
import com.example.examplerest.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Service
public class AuthorServiceTest {

    @Autowired
    private AuthorService authorService;

    @MockBean
    private AuthorRepository authorRepository;

    @BeforeEach
    public void beforeAll() {
        authorRepository.deleteAll();
    }

//    @Test
//    public void save() {
//        Author author = Author.builder()
//                .id(1)
//                .name("authot")
//                .surname("author")
//                .email("author@gmail.com")
//                .build();
//
//        when(authorRepository.save(any())).thenReturn(author);
//
//        authorService.save(Author.builder()
//                .name("authot")
//                .surname("author")
//                .email("author@gmail.com")
//                .build());
//
//        verify(authorRepository, times(1)).save(any());
//    }

//    @Test
//    public void findAll() {
//        testAddAuthors();
//
//        List<Author> actual = authorService.findAll();
//        List<Author> expected = authorRepository.findAll();
//        assertEquals(expected, actual);
//    }

    private void testAddAuthors() {
        authorRepository.save(Author.builder()
                .name("author1")
                .surname("author1")
                .email("author1@gmail.com")
                .gender(Gender.FEMALE)
                .build());
        authorRepository.save(Author.builder()
                .name("author2")
                .surname("author2")
                .email("author2@gmail.com")
                .gender(Gender.MALE)
                .build());
    }

}
