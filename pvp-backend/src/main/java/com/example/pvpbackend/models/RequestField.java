package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Entity
@Table(name = "laukas")
public class RequestField {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_laukas")
    private Integer id;

    private String etikete;
    private String tipas;
    private Boolean privalomas;
    private String pasirinkimai;
    @Column(name = "eil_nr")
    private Integer eilNr;

    @OneToOne
    @JoinColumn(name = "fk_prasymasid_prasymas")
    private Request request;

    public RequestField(){}
    public RequestField(String etikete, String tipas, Boolean privalomas, String pasirinkimai, Integer eilNr, Request request){
        this.etikete = etikete;
        this.tipas = tipas;
        this.privalomas = privalomas;
        this.pasirinkimai =pasirinkimai;
        this.eilNr = eilNr;
        this.request = request;
    }
}
