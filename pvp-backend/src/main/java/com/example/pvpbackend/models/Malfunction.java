package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "gedimas")
@NoArgsConstructor
@AllArgsConstructor
public class Malfunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Gedimas")
    private Integer id;

    private String aprasymas;
    private String tipas;

    private LocalDateTime uzregistravimoData;

    @Column(name = "adresas", columnDefinition = "TEXT")
    private String addressJson;


    @OneToOne
    @JoinColumn(name = "fk_Uzregistruoti_gedimaiid_Uzregistruoti_gedimai")
    private RegisteredMalfunctions registeredMalfunctions;
}
