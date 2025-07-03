package com.example.pvpbackend.repositories;

import com.example.pvpbackend.models.MalfunctionFiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MalfunctionFileRepository extends JpaRepository<MalfunctionFiles, Long> {
    List<MalfunctionFiles> findByGedimasId(Integer gedimasId);
}