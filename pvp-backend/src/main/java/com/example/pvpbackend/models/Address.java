package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity
@Table(name = "adresas")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Adresas", nullable = false)
    private Integer id;

    @Column(name = "miestas")
    private String seniunija;

    private String gatve;

    @Column(name="namo_numeris")
    private int namoNumeris;

    @ManyToOne
    @JoinColumn(name = "fk_Gyventojasid_Naudotojas", referencedColumnName = "id_Naudotojas")
    private Client gyventojas;

    public Address(){};

    public Address(String seniunija, String gatve, int namoNumeris, Client gyventojas) {
        this.seniunija = seniunija;
        this.gatve = gatve;
        this.namoNumeris = namoNumeris;
        this.gyventojas = gyventojas;
    }
}
