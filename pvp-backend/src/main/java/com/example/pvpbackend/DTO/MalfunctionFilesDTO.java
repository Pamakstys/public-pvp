package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MalfunctionFilesDTO {
    private Long id;
    private String pavadinimas;
    private String downloadUrl;
}
