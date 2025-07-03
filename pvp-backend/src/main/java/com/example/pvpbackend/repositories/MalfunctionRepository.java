package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Employee;
import com.example.pvpbackend.models.Malfunction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MalfunctionRepository extends JpaRepository<Malfunction, Integer> {
    long countByRegisteredMalfunctions_Darbuotojas(Employee darbuotojas);
}
