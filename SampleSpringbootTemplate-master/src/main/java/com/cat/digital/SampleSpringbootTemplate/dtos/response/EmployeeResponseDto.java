package com.cat.digital.SampleSpringbootTemplate.dtos.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EmployeeResponseDto {
    private String id;
    private String employeeName;
    private String employeeSalary;
    private String employeeAge;
    private String employeeImage;
}
