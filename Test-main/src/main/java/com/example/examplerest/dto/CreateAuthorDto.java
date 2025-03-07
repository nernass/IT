package com.example.examplerest.dto;

import com.example.examplerest.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAuthorDto {

    private String name;
    private String surname;
    private String email;
    @Enumerated(EnumType.STRING)
    private Gender gender;


}
