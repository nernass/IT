package com.example.examplerest.service;

import com.example.examplerest.exception.EntityNotFoundException;
import com.example.examplerest.model.Book;
import com.example.examplerest.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public Book save(Book book) {

//        bookRepository.flush(); չի սպասում, որ մի քանի user-ներ save լինեն, որ
//        մեկ անգամ տանի բազայում բոլորին save անի, այլ միանգամից էդ մեկին
//        save է անում։

        return bookRepository.save(book);
    }

    public Optional<Book> findById(int id) {
//        this.save()  եթե երկու մեթոդներ ունեն transactional աշխատում է
//        վերևինը, եթե վերևի մեթոդը կանչել ենք ներքևի մեթոդում, ապա կապ չունի
//        ունի transactional մեթոդը, թե ոչ կանչվում է վեևինինը։
//        Եթե this-ով ինչ-որ մեթոդ մյուս մեթոդի մեջ է կանչված, ապա կանչողի
//        transactional-ը ignore  է լինում։
        return bookRepository.findById(id);
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }
}
