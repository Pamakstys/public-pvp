package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity
@Table(name = "naudotojas")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Naudotojas")
    private Integer id;

    private String vardas;

    private String pavarde;

    @Column(name = "el_pastas")
    private String elPastas;

    @Column(name = "tel_numeris")
    private String telNumeris;

    private String slaptazodis;

    @Transient
    private String token;

    @Transient
    private String role;

    public User(){

    }

    public User(String vardas, String pavarde, String elPastas, String slaptazodis){
        this.vardas = vardas;
        this.pavarde = pavarde;
        this.elPastas = elPastas;
        this.slaptazodis = slaptazodis;
    }

}
