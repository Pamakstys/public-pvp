package com.example.pvpbackend.DTO;

import com.example.pvpbackend.models.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientListDTO {
    private Integer id;
    private String vardas;
    private String pavarde;
    private List<Address> addressList;
}
