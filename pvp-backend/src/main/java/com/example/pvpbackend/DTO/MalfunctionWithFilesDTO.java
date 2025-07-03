package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MalfunctionWithFilesDTO {
    private Integer id;
    private String aprasymas;
    private String tipas;
    private LocalDateTime uzregistravimoData;
    private String addressJson;
    private List<MalfunctionFilesDTO> files;
}

