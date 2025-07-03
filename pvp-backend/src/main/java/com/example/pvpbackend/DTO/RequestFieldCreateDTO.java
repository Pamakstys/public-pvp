package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestFieldCreateDTO {
    private String label;
    private String type;
    private Boolean required;
    private String options;
    private Integer position;
}
