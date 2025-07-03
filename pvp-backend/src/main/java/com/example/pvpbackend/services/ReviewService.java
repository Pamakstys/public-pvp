package com.example.pvpbackend.services;

import com.example.pvpbackend.DTO.ReviewDTO;
import com.example.pvpbackend.DTO.ReviewSummaryDTO;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.Review;
import com.example.pvpbackend.repositories.ReviewRepository;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.time.LocalDate;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UsersService usersService;
    public ReviewService(ReviewRepository reviewRepository, UsersService usersService){
        this.reviewRepository = reviewRepository;
        this.usersService = usersService;
    }

    public void create(ReviewDTO reviewDTO){
        Client client = usersService.getClient(usersService.getCurrent());
        String aprasymas = reviewDTO.getAprasymas() == null ? "" : reviewDTO.getAprasymas();

        Review review = new Review();
        review.setGyventojas(client);
        review.setIvertinimas(reviewDTO.getIvertinimas());
        review.setAprasymas(aprasymas);
        LocalDate today = LocalDate.now();
        Date sqlDate = Date.valueOf(today);
        review.setData(sqlDate);
        reviewRepository.save(review);
    }
    public List<Review> getAll(){
        return reviewRepository.findAll();
    }

    public List<ReviewSummaryDTO> getRatings(Date fromDate) {
        List<Object[]> rawResults;

        if (fromDate != null) {
            rawResults = reviewRepository.getRatingSummaryFromDate(fromDate);
        } else {
            rawResults = reviewRepository.getRatingSummaryAll();
        }
        return rawResults.stream()
                .map(obj -> new ReviewSummaryDTO(
                        ((Number) obj[0]).intValue(),
                        ((Number) obj[1]).intValue()
                ))
                .collect(Collectors.toList());
    }

}
