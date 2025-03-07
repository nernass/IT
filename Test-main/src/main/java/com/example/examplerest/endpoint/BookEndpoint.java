package com.example.examplerest.endpoint;

import com.example.examplerest.dto.BookDto;
import com.example.examplerest.exception.EntityNotFoundException;
import com.example.examplerest.mapper.BookMapper;
import com.example.examplerest.model.Book;
import com.example.examplerest.repository.BookRepository;
import com.example.examplerest.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class BookEndpoint {

    private final RestTemplate restTemplate;

    private final BookService bookService;

    private final BookMapper bookMapper;

//    List<Book> books = new ArrayList<>(List.of(
//            new Book(1,"girq1","poxos",34.5, BookLanguage.EN),
//            new Book(2,"girq2","petros",34.5, BookLanguage.ARM),
//            new Book(3,"girq3","martiros",34.5, BookLanguage.RU)
//    ));

    @GetMapping("/books")
    public ResponseEntity<List<BookDto>> getAllBooks() {
        List<Book> all = bookService.findAll();
        if (!all.isEmpty()) {
            ResponseEntity<HashMap> currency = restTemplate.getForEntity("https://cb.am/latest.json.php?currency=USD", HashMap.class);
            HashMap<String, String> hashMap = currency.getBody();
            if (!hashMap.isEmpty()) {
                double usdCurrency = Double.parseDouble(hashMap.get("USD"));
                if (usdCurrency > 0) {
                    for (Book book : all) {
                        double price = book.getPrice() / usdCurrency;
                        DecimalFormat df = new DecimalFormat("#.##");
                        book.setPrice(Double.parseDouble(df.format(price)));
                    }
                }
            }
        }
        return ResponseEntity.ok(bookMapper.map(all));
    }

//    @GetMapping("/books/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
//        if(!bookRepository.existsById(id)) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(bookRepository.findById(id).get());
//    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Optional<Book>> getBookById(@PathVariable("id") int id) throws EntityNotFoundException {
        return ResponseEntity.ok(bookService.findById(id));

//        @GetMapping("/books/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
//        Optional<Book> byId = bookRepository.findById(id);
//        return byId.map(ResponseEntity::ok).orElseGet(()-> ResponseEntity.notFound().build());

//      @GetMapping("/books/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable("id") int id) {
//        Optional<Book> byId = bookRepository.findById(id);
//        if (byId.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(byId.get());
//    }
//
    }

    @PostMapping("/books")
    public ResponseEntity<?> createBook(@Valid @RequestBody Book book) {
        bookService.save(book);
        return ResponseEntity.noContent().build();
    }
}


//        for (Book bookFromDB : books) {
//            if(bookFromDB.getTitle().equals(book.getTitle())
//                    && bookFromDB.getAuthorName().equals(book.getAuthorName())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT)
//                        .build();
//            }
//        }
//        books.add(book);


//        @PutMapping("/books")
//        public ResponseEntity<Book> updateBook (@RequestBody Book book){
//            if (book.getId() == 0) {
//                return ResponseEntity.badRequest().build();
//            }
//            bookRepository.save(book);
//            return ResponseEntity.ok(book);
//        }
//


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


//        @DeleteMapping("/books/{id}")
//        public ResponseEntity<?> deleteBookById ( @PathVariable("id") int id){
//            bookRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }


//        for (Book book : books) {
//            if(book.getId() == id) {
//                books.remove(book);
//                return ResponseEntity.noContent().build();
//            }
//        }


