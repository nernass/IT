package com.example.examplerest.endpoint;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.model.BookLanguage;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class BookEndpointTest {

    @Autowired
    private BookRepository bookRepository;

    private static final String BASE_URL = "http://localhost:8080";

    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllBooks() throws Exception {
        testAddBooks();

        mvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void getBookById() throws Exception {
        testAddBooks();
        ////idddd????
        mvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/books/{id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void createBook() throws Exception {
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("title", "book");
        objectNode.put("description", "desc");
        objectNode.put("price", 1561.0);

        mvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectNode.toString()))
                .andExpect(status().isNoContent());
    }

    private void testAddBooks() {
        bookRepository.save(Book.builder()
                .author(new Author())
                .title("book")
                .description("book")
                .price(4.156)
                .language(BookLanguage.EN)
                .build());
        bookRepository.save(Book.builder()
                .author(new Author())
                .title("book")
                .description("book")
                .price(4.156)
                .language(BookLanguage.ARM)
                .build());
    }
}