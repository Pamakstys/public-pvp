package com.example.pvpbackend.models;

import com.example.pvpbackend.controllers.EmployeeController;
import com.example.pvpbackend.enums.RegisteredRequestType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@Entity
@Table(name = "uzregistruotas_prasymas")
public class RegisteredRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_uzregistruotas_prasymas")
    private Integer id;

    private String duomenys;
    private String pavadinimas;
    private String aprasymas;
    private LocalDateTime data;
    private Integer busena;

    @ManyToOne
    @JoinColumn(name = "fk_Gyventojasid_Naudotojas", referencedColumnName = "id_Naudotojas")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "fk_prasymasid_prasymas")
    private Request request;

    @ManyToOne
    @JoinColumn(name = "fk_Darbuotojasid_Naudotojas", referencedColumnName = "id_Naudotojas")
    private Employee employee;

    public RegisteredRequest(){}
    public RegisteredRequest(String duomenys, String pavadinimas, String aprasymas, LocalDateTime data, Integer busena, Client client, Request request, Employee employee){
        this.duomenys = duomenys;
        this.pavadinimas = pavadinimas;
        this.aprasymas = aprasymas;
        this.data = data;
        this.busena = busena;
        this.client = client;
        this.request = request;
        this.employee = employee;
    }

    public RegisteredRequestType getBusenaEnum() {
        if (busena == null) return null;
        return RegisteredRequestType.values()[busena-1];
    }

    public void setBusenaEnum(RegisteredRequestType type) {
        if (type == null) {
            this.busena = null;
        } else {
            this.busena = type.ordinal();
        }
    }
}
