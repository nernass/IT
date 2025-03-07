package com.example.examplerest.mapper;

import com.example.examplerest.dto.AuthorResponseDto;
import com.example.examplerest.dto.CreateAuthorDto;
import com.example.examplerest.model.Author;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    Author map(CreateAuthorDto createAuthorDto);
    AuthorResponseDto map(Author author);

    List<AuthorResponseDto> map(List<Author> authorList);

}
