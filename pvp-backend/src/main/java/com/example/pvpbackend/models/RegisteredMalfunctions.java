package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "uzregistruoti_gedimai")
public class RegisteredMalfunctions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Uzregistruoti_gedimai")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_Gyventojasid_Naudotojas")
    private Client gyventojas;

    @ManyToOne(optional = true)
    @JoinColumn(name = "fk_Darbuotojasid_Naudotojas")
    private Employee darbuotojas;

    @OneToOne(mappedBy = "registeredMalfunctions")
    private Malfunction gedimas;

    public Malfunction getGedimas() {
        return gedimas;
    }
}
