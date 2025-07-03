package com.example.pvpbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
@Entity
@Table(name = "darbuotojas")
public class Employee {
    @Id
    @Column(name = "id_Naudotojas", nullable = false)
    private Integer idNaudotojas;

    private String role;

    @Getter
    @Setter
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_Naudotojas")
    private User user;

    @OneToMany(mappedBy = "employee")
    private List<RegisteredRequest> registeredRequests;


    public Employee() {
    }

    public Employee(Integer idNaudotojas){
        this.idNaudotojas = idNaudotojas;
    }
}
