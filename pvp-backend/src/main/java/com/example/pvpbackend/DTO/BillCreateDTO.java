package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillCreateDTO {
    private String imones_pavadinimas;
    private LocalDateTime mokejimo_data;
    private LocalDateTime sumoketi_iki;
    private LocalDateTime gavimo_data;
    private String iban;
    private Long suma;
    private String imokos_kodas;
    private String aprasymas;
    private Long sumoketa_suma;
    private String json;

    private Integer addressId;
    private Integer clientId;
}
