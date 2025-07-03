package com.example.pvpbackend.repositories;


import com.example.pvpbackend.DTO.ReviewSummaryDTO;
import com.example.pvpbackend.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT r.ivertinimas, COUNT(r) FROM Review r WHERE r.data >= :fromDate GROUP BY r.ivertinimas")
    List<Object[]> getRatingSummaryFromDate(@Param("fromDate") Date fromDate);

    @Query("SELECT r.ivertinimas, COUNT(r) FROM Review r GROUP BY r.ivertinimas")
    List<Object[]> getRatingSummaryAll();

}