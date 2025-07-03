package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.AddressCreateDTO;
import com.example.pvpbackend.models.Address;
import com.example.pvpbackend.models.Contract;
import com.example.pvpbackend.models.ScheduleDate;
import com.example.pvpbackend.services.AddressService;
import com.example.pvpbackend.services.ScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/schedule")
@Slf4j
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final AddressService addressService;
    public ScheduleController(ScheduleService scheduleService, AddressService addressService){
        this.scheduleService = scheduleService;
        this.addressService = addressService;
    }

    @PostMapping("/get")
    public ResponseEntity<Object> showSchedules(@RequestBody AddressCreateDTO addressDTO){
        List<Contract> contracts = scheduleService.getFormatedSchedules(addressDTO.getAddress(), addressDTO.getHouseNumber(), addressDTO.getSubDistrict());
        if(contracts == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to get schedules");
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(contracts);
    }
    @GetMapping("/download-schedule/{id}")
    public ResponseEntity<Object> downloadSchedule(@PathVariable int id) {
        try {
            byte[] zip = scheduleService.generateZip(id);
            if(zip == null){
                Map<String, String> response = new HashMap<>();
                response.put("error", "Failed to download schedule");
                return ResponseEntity.badRequest().body(response);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("schedules.zip").build());
            return new ResponseEntity<>(zip, headers, HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to download schedule");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
