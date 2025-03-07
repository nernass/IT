package com.example.examplerest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "name can not be empty")
    @NotNull(message = "name can not be null")
    private String name;
    private int age;
    private String surname;
    @NotEmpty(message = "email can not be empty")
    @NotNull(message = "email can not be null")
    private String email;
    @NotEmpty(message = "password can not be empty")
    @NotNull(message = "password can not be null")
    private String password;
    @NotNull(message = "name can not be null")
    @Enumerated(EnumType.STRING)
    private Role role;


}
