package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.sql.Date;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@Entity
@Table(name = "saskaita")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Saskaita", nullable = false)
    private Integer id;
    private String imones_pavadinimas;
    private LocalDateTime mokejimo_data;
    private LocalDateTime sumoketi_iki;
    private LocalDateTime gavimo_data;
    private String iban;
    private Long suma;
    private String imokos_kodas;
    private String aprasymas;
    private Long sumoketa_suma;
    private Boolean sumoketa;
    private String json;

    @ManyToOne
    @JoinColumn(name = "fk_Adresasid_Adresas", referencedColumnName = "id_Adresas")
    private Address adresas;

    @ManyToOne
    @JoinColumn(name = "fk_Gyventojasid_Naudotojas", referencedColumnName = "id_Naudotojas")
    private Client gyventojas;

    @Transient
    private Double selectedAmountToPay;

    public Bill(){};
    public Bill(String imones_pavadinimas, LocalDateTime mokejimo_data, LocalDateTime sumoketi_iki, LocalDateTime gavimo_data, String iban, Long suma, String imokos_kodas, String aprasymas, Long sumoketa_suma, Boolean sumoketa, String json, Address adresas, Client gyventojas){
        this.imones_pavadinimas = imones_pavadinimas;
        this.mokejimo_data = mokejimo_data;
        this.sumoketi_iki = sumoketi_iki;
        this.gavimo_data = gavimo_data;
        this.iban = iban;
        this.suma = suma;
        this.imokos_kodas = imokos_kodas;
        this.aprasymas = aprasymas;
        this.sumoketa_suma = sumoketa_suma;
        this.sumoketa = sumoketa;
        this.json = json;
        this.adresas = adresas;
        this.gyventojas = gyventojas;
    }
}
