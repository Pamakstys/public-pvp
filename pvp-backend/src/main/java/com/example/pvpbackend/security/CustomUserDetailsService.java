package com.example.pvpbackend.security;


import com.example.pvpbackend.enums.UserRole;
import com.example.pvpbackend.repositories.ClientRepository;
import com.example.pvpbackend.repositories.EmployeeRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.pvpbackend.services.UsersService;
import com.example.pvpbackend.models.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersService userService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.list().stream()
                .filter(u -> u.getElPastas().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        String role = determineUserRole(user.getId());

        return new org.springframework.security.core.userdetails.User(user.getElPastas(), user.getSlaptazodis(), mapRoleToAuthorities(role));
    }

    private List<GrantedAuthority> mapRoleToAuthorities(String role) {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    public User loadLoggedInUser(String username) {
        User user = userService.list().stream()
                .filter(u -> u.getElPastas().equals(username))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        user.setRole(determineUserRole(user.getId()));
        return user;
    }

    private String determineUserRole(Integer userId) {
        if (clientRepository.existsById(userId)) {
            return "ROLE_CLIENT";
        } else if (employeeRepository.existsById(userId)) {
            return "ROLE_EMPLOYEE";
        }
        return "ROLE_USER";
    }
}
