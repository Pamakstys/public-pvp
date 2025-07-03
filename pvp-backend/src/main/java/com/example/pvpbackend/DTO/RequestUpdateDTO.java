package com.example.pvpbackend.DTO;

import com.example.pvpbackend.models.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateDTO {
    private Integer id;
    private String pavadinimas;
    private String aprasymas;
    private String role;
    private Date data;
    private List<RequestFieldUpdateDTO> fields;
}

