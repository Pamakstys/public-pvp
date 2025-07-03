package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@Entity
@Table(name = "gyventojas")
public class Client {
    @Id
    @Column(name = "id_Naudotojas", nullable = false)
    private Integer idNaudotojas;

    @Column(name = "el_pasto_priminimai")
    private Boolean elPastoPriminimai;

    @Column(name = "tel_numerio_priminimai")
    private Boolean telNumerioPriminimai;

    @Column(name = "atnaujinti_duomenys")
    private Date atnaujintiDuomenys;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_Naudotojas")
    private User user;

    @OneToMany(mappedBy = "gyventojas", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> adresai = new ArrayList<>();

    public Client(){

    }
    public Client(Integer idNaudotojas){
        this.idNaudotojas = idNaudotojas;
    }
}
