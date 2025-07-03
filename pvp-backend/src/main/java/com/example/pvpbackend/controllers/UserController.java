package com.example.pvpbackend.controllers;

import com.example.pvpbackend.DTO.*;
import com.example.pvpbackend.models.Client;
import com.example.pvpbackend.models.User;
import com.example.pvpbackend.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UsersService userService;
    public UserController(UsersService usersService){
        this.userService = usersService;
    }

    @PostMapping("/update-user")
    public ResponseEntity<Object> updateUserData(@RequestBody UserClientDataUpdateDTO updateDTO){
        try {
            userService.updateClientUser(updateDTO);
            return ResponseEntity.ok("User updated");
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to update user data");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/get-client")
    public ResponseEntity<Object> getClient(){
        try {
            UserClientDataUpdateDTO dataDTO = userService.getUserClient();
            return ResponseEntity.ok(dataDTO);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to fetch client");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/data-update-date")
    public ResponseEntity<Object> getClientDataUpdateDate(){
        try {
            Date date = userService.getClientUpdateDate();
            Map<String, Object> response = new HashMap<>();
            if(date == null) {
                response.put("success", "No date found");
                return ResponseEntity.ok(response);
            }
            response.put("date", date);
            return ResponseEntity.ok(response);
        } catch(Exception e){
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to fetch date");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
