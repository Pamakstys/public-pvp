package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Data
@Entity
@Table(name = "prasymas")
public class Request{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prasymas")
    private Integer id;
    private Date data;
    private String pavadinimas;
    private String aprasymas;
    private String failas;
    private String role;
    private Boolean aktyvus;
    public Request(){}
    public Request(Date data, String pavadinimas, String aprasymas, String failas, String role, Boolean aktyvus) {
        this.data = data;
        this.pavadinimas = pavadinimas;
        this.aprasymas = aprasymas;
        this.failas = failas;
        this.role = role;
        this.aktyvus = aktyvus;
    }
}
