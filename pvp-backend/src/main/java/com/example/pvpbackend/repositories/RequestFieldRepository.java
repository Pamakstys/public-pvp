package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.Request;
import com.example.pvpbackend.models.RequestField;
import jakarta.persistence.OrderBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestFieldRepository extends JpaRepository<RequestField, Integer> {

    Optional<List<RequestField>> findByRequestOrderByEilNrAsc(Request request);
}
