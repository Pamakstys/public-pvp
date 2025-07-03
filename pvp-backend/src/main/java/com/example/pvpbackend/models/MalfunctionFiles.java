package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "failai")
@NoArgsConstructor
@AllArgsConstructor
public class MalfunctionFiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Failai")
    private Integer id;

    private String fizinisKelias;
    private String pavadinimas;

    @ManyToOne
    @JoinColumn(name = "fk_Gedimasid_Gedimas")
    private Malfunction gedimas;
}
