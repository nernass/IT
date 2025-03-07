package com.example.examplerest.mapper;

import com.example.examplerest.dto.BookDto;
import com.example.examplerest.model.Book;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")

public interface BookMapper {

    BookDto map(Book book);

    List<BookDto> map(List<Book> books);

}
