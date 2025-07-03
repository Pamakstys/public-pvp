package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Bill;
import com.example.pvpbackend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {
    List<Bill> findAllByGyventojasAndSumoketa(Client gyventojas, boolean sumoketa);
}
