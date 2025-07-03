package com.example.pvpbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataUpdateDTO {
    private String vardas;
    private String pavarde;
    private String elPastas;
    private String telNumeris;
}
