package com.example.examplerest.dto;

import com.example.examplerest.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFilterDto {

    private String name;
    private String surname;
    private String email;
    private Role role;

}
