package com.example.examplerest.dto;
import com.example.examplerest.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestErrorDto {

    private int statusCode;
    private String errorMessage;

}
