package com.example.examplerest.dto;

import com.example.examplerest.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorResponseDto {

    private int id;
    private String name;
    private String surname;
    private Gender gender;

}
