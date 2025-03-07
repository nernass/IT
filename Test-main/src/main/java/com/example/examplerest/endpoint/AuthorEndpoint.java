package com.example.examplerest.endpoint;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.mapper.AuthorMapper;
import com.example.examplerest.model.Author;
import com.example.examplerest.model.Book;
import com.example.examplerest.repository.AuthorRepository;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.security.CurrentUser;
import com.example.examplerest.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorEndpoint {

    private final AuthorService authorService;
    private final BookRepository bookRepository;
    private final AuthorMapper authorMapper;
    Logger log = LoggerFactory.getLogger(AuthorEndpoint.class.getName());

//    List<Book> books = new ArrayList<>(List.of(
//            new Book(1,"girq1","poxos",34.5, BookLanguage.EN),
//            new Book(2,"girq2","petros",34.5, BookLanguage.ARM),
//            new Book(3,"girq3","martiros",34.5, BookLanguage.RU)
//    ));

    @Operation(
            operationId = "getAllAuthors",
            summary = "Get all authors",
            description = "Get all authors description"
    )
    @GetMapping("/authors")
    public ResponseEntity<List<AuthorResponseDto>> getAllAuthors(@AuthenticationPrincipal CurrentUser currentUser) {
//        log.info("endpoint /authors called by {}", currentUser.getUser().getName());
        return ResponseEntity.ok(authorMapper.map(authorService.findAll()));
    }

//        return AuthorConverter.convertEntitiesToResponseDtos(authorRepository.findAll());
//    @GetMapping("/books/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
//        if(!bookRepository.existsById(id)) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(bookRepository.findById(id).get());
//    }

//    @GetMapping("/books/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
//        Optional<Book> byId = bookRepository.findById(id);
//        if(byId.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(byId.get());
//    }

    @PostMapping("/authors")
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody CreateAuthorDto createAuthorDto) {
        return ResponseEntity.ok(authorService.save(authorMapper.map(createAuthorDto)));
    }
        //        for (Book bookFromDB : books) {
//            if(bookFromDB.getTitle().equals(book.getTitle())
//                    && bookFromDB.getAuthorName().equals(book.getAuthorName())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT)
//                        .build();
//            }
//        }
//        books.add(book);
//        Author savedAuthor = authorRepository.save(AuthorConverter.convertDtoToAuthor(createAuthorDto));


    @PutMapping("/books")
    public ResponseEntity<Book> updateBook(@RequestBody Book book) {
//        if(book.getId() > 0) {
//            for (Book bookFromDB : books) {
//                if(bookFromDB.getId() == book.getId()) {
//                    bookFromDB.setLanguage(book.getLanguage());
//                    bookFromDB.setTitle(book.getTitle());
//                    bookFromDB.setPrice(book.getPrice());
//                    bookFromDB.setAuthorName(book.getAuthorName());
//                    return ResponseEntity.ok(bookFromDB);
//                }
//            }
//        }
        if(book.getId() == 0) {
            return ResponseEntity.badRequest().build();
        }
        bookRepository.save(book);
        return ResponseEntity.ok(book);
    }
}

//    @DeleteMapping("/books/{id}")
//    public ResponseEntity<?> deleteBookById(@PathVariable("id") int id) {
////        for (Book book : books) {
////            if(book.getId() == id) {
////                books.remove(book);
////                return ResponseEntity.noContent().build();
////            }
////        }
//        bookRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }

