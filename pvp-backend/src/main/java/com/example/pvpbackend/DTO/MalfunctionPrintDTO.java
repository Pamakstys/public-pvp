package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MalfunctionPrintDTO {

        private Integer id;
        private String aprasymas;
        private String tipas;
        private LocalDateTime uzregistravimoData;
        private String gyventojasVardas;
        private String addressJson;
}
