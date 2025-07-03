package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    List<Address> findByGyventojas(Client gyventojas);

}
