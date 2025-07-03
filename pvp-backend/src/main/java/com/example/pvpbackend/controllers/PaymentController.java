package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.PaymentRequestDTO;
import com.example.pvpbackend.models.Bill;
import com.example.pvpbackend.services.BillService;
import com.example.pvpbackend.services.StripeService;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/payment")
public class PaymentController {
    private final StripeService stripeService;
    private final BillService billService;
    public PaymentController(StripeService stripeService, BillService billService){
        this.stripeService = stripeService;
        this.billService = billService;
    }

    @PostMapping("/validate-bills")
    public ResponseEntity<Object> validateBills(@RequestBody PaymentRequestDTO requestDTO) {
        List<Bill> bills = billService.getBills(requestDTO.getItems());
        if (bills == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/get-bills")
    public ResponseEntity<Object> getBills(@RequestParam(defaultValue = "false") Boolean paid){
        try {
            List<Bill> bills = billService.getBillsByPaid(paid);
            return ResponseEntity.ok(bills);
        } catch (Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to fetch bills");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create-session")
    public ResponseEntity<Object> createStripeSession(@RequestBody PaymentRequestDTO requestDTO) {
        List<Bill> bills = billService.getBills(requestDTO.getItems());
        if (bills == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid bill selection");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            String url = stripeService.createCheckoutSession(bills);
            return ResponseEntity.ok(Collections.singletonMap("url", url));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Stripe API error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create session");
        }
    }

}
