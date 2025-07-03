package com.example.pvpbackend.controllers;

import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.services.AddressService;
import com.example.pvpbackend.services.EmailService;
import com.example.pvpbackend.services.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@Slf4j
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private ScheduleService scheduleService;
    private final AddressService addressService;
    public EmailController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping("/send-schedule/{id}")
    public ResponseEntity<Object> sendScheduleZip(@PathVariable int id) {
        try {
            Map<String, String> response = new HashMap<>();
            Address address = addressService.getWithClientCheck(id);
            if(address == null){
                response.put("error", "Not clients address");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            byte[] zipBytes = scheduleService.generateZip(id);
            emailService.sendEmailWithZip(zipBytes, "grafikai.zip");
            response.put("success", "Email sent");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send schedule");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
