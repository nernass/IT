package com.example.examplerest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "author")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "name can not be empty")
    @NotNull(message = "name can not be null")
    private String name;
    private String surname;
    @NotEmpty(message = "email can not be empty")
    @NotNull(message = "email can not be null")
    private String email;
    @NotNull(message = "gender can not be null")
    @Enumerated(EnumType.STRING)
    private Gender gender;

//    @JsonIgnore
//    public String getEmail() {
//        return email;
//    }
//
//    @JsonProperty
//    public void setEmail(String email) {
//        this.email = email;
//    }

}
