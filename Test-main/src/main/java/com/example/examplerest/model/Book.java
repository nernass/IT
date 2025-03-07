package com.example.examplerest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "title cant be null or empty")
    private String title;
//    @JsonProperty("author_name")
//    private String authorName;
    private String description;
//    @Min(0)
    @Range(min = 0, max = 1000)
//    @Size(min = 0, max = 1000)
    private double price;
    @Enumerated(EnumType.STRING)
    private BookLanguage language;
    @NotNull(message = "author cant be null")
    @ManyToOne
    private Author author;


}
