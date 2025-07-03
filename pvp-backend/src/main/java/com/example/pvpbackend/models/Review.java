package com.example.pvpbackend.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@Data
@Entity
@Table(name = "atsiliepimas")

public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Atsiliepimas")
    private Integer id;

    private String aprasymas;

    private Integer ivertinimas;

    private Date data;
    @ManyToOne
    @JoinColumn(name = "fk_Gyventojasid_Naudotojas", referencedColumnName = "id_Naudotojas")
    private Client gyventojas;

    public Review() {

    }
    public Review(String aprasymas, Integer ivertinimas, Date data, Client gyventojas) {
        this.aprasymas = aprasymas;
        this.ivertinimas = ivertinimas;
        this.data = data;
        this.gyventojas = gyventojas;
    }


}
