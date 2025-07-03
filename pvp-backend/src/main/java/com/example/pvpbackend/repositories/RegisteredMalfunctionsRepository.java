package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.RegisteredMalfunctions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisteredMalfunctionsRepository  extends JpaRepository<RegisteredMalfunctions, Integer> {
    List<RegisteredMalfunctions> findByGyventojas(Client gyventojas);
}
