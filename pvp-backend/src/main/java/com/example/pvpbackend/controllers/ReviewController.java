package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.ReviewDTO;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.Employee;
import com.example.pvpbackend.models.Review;
import com.example.pvpbackend.models.User;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.EmployeeRepository;
import com.example.pvpbackend.repositories.ReviewRepository;
import com.example.pvpbackend.repositories.UserRepository;
import com.example.pvpbackend.services.ReviewService;
import com.example.pvpbackend.services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final UsersService usersService;
    public ReviewController(ReviewService reviewService, UsersService usersService){
        this.reviewService = reviewService;
        this.usersService = usersService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> createReview(@RequestBody ReviewDTO reviewDTO) {
        try {
            Map<String, String> response = new HashMap<>();
            if (reviewDTO.getIvertinimas() < 1 || reviewDTO.getIvertinimas() > 5) {
                response.put("error", "Įvertinimas turi būti nuo 1 iki 5");
                return ResponseEntity.badRequest().body(response);
            }
            reviewService.create(reviewDTO);
            response.put("success", "Review created");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to create reviews");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllReviews() {
        try {
            User user = usersService.getCurrent();
            Employee employee = usersService.getEmployee(user);

            if (employee == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Unauthorized");
                return ResponseEntity.badRequest().body(response);
            }
            List<Review> allReviews = reviewService.getAll();
            List<ReviewDTO> reviewDTOs = allReviews.stream()
                    .map(r -> new ReviewDTO(r.getIvertinimas(), r.getAprasymas()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(reviewDTOs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to get reviews");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
