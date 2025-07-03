package com.example.pvpbackend.controllers;

import com.example.pvpbackend.models.*;
import com.example.pvpbackend.DTO.*;
import com.example.pvpbackend.security.CustomUserDetailsService;
import com.example.pvpbackend.security.TokenBlacklistService;
import com.example.pvpbackend.services.UsersService;
import com.example.pvpbackend.utils.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
@Slf4j
public class AuthorizationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;
    @Autowired
    public AuthorizationController(){
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody UserDTO userDTO){
        try {
            User user = userDTO.getUser();
            String userType = userDTO.getType();
            Map<String, String> clientResponse = new HashMap<>();

            if(usersService.isEmailTaken(user.getElPastas())) {
                clientResponse.put("message", "Email already exists");
                return ResponseEntity.ok(clientResponse);
            }
            User savedUser = usersService.create(user);

            switch (userType.toLowerCase()) {
                case "worker":
                    Employee employee = usersService.createEmployee(savedUser.getId());
                    clientResponse.put("message", "You have registered successfully (Employee)");
                    return ResponseEntity.ok(clientResponse);
                case "client":  
                    Client client = usersService.createClient(savedUser.getId());
                    clientResponse.put("message", "You have registered successfully (Client)");
                    return ResponseEntity.ok(clientResponse);
                default:
                    clientResponse.put("message", "Invalid user type");
                    return ResponseEntity.ok(clientResponse);
            }

        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok("You have been logged out successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<User> createAuthenticationToken(@RequestBody loginDTO credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getEmail(), credentials.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (userDetails == null) {
                return ResponseEntity.badRequest().build();
            }
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            User user = userDetailsService.loadLoggedInUser(userDetails.getUsername());

            user.setToken(jwt);
            user.setSlaptazodis(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
