package com.cat.digital.SampleSpringbootTemplate.clients.dto.response;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DummyEmployeeResponse {
    private String status;

    @Builder.Default
    private List<DummyEmployee> data = new ArrayList<>();
}
