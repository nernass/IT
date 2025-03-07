package com.example.examplerest.dto;

import com.example.examplerest.model.Author;
import com.example.examplerest.model.BookLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDto {

    private int id;
    private String title;
    private String description;
    private double price;
    @Enumerated(EnumType.STRING)
    private BookLanguage language;


}
