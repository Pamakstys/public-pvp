package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestFieldUpdateDTO {
    private Integer id;
    private String etikete;
    private String tipas;
    private Boolean privalomas;
    private String pasirinkimai;
    private Integer eilNr;
}
