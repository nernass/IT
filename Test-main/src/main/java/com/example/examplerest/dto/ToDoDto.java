package com.example.examplerest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ToDoDto {

    private int id;
    private int userId;
    private String title;
    private boolean completed;

}
