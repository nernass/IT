//package com.example.examplerest.converter;
//
//import com.example.examplerest.dto.AuthorResponseDto;
//import com.example.examplerest.dto.CreateAuthorDto;
//import com.example.examplerest.model.Author;
//import lombok.experimental.UtilityClass;
//
//import java.util.ArrayList;
//import java.util.List;
//@UtilityClass
//public class AuthorConverter {
//
//    public Author convertDtoToAuthor(CreateAuthorDto createAuthorDto) {
//        return Author.builder()
//                .name(createAuthorDto.getName())
//                .surname(createAuthorDto.getSurname())
//                .email(createAuthorDto.getEmail())
//                .gender(createAuthorDto.getGender())
//                .build();
//    }
//
//    public AuthorResponseDto convertEntityToResponseDto(Author author) {
//        return AuthorResponseDto.builder()
//                .id(author.getId())
//                .name(author.getName())
//                .surname(author.getSurname())
//                .gender(author.getGender())
//                .build();
//    }
//
//    public List<AuthorResponseDto> convertEntitiesToResponseDtos(List<Author> authors) {
//        List<AuthorResponseDto> authorResponseDtoList = new ArrayList<>();
//        for (Author author : authors) {
//            authorResponseDtoList.add(convertEntityToResponseDto(author));
//        }
//        return authorResponseDtoList;
//    }
//}
