package com.example.pvpbackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/logout").permitAll()
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/webhook").permitAll()

                        .requestMatchers("/api/user").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/api/user/**").hasAnyRole("CLIENT", "EMPLOYEE")

                        .requestMatchers("/api/email").hasRole("CLIENT")
                        .requestMatchers("/api/email/**").hasRole("CLIENT")

                        .requestMatchers("/api/schedule").hasRole("CLIENT")
                        .requestMatchers("/api/schedule/**").hasRole("CLIENT")

                        .requestMatchers("/api/address").hasRole("CLIENT")
                        .requestMatchers("/api/address/**").hasRole("CLIENT")

                        .requestMatchers("/api/employee").hasRole("EMPLOYEE")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")

                        .requestMatchers("/api/payment").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/api/payment/**").hasAnyRole("CLIENT", "EMPLOYEE")

                        .requestMatchers("/api/request").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/api/request/**").hasAnyRole("CLIENT", "EMPLOYEE")

                        .requestMatchers("/api/review").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/api/review/**").hasAnyRole("CLIENT", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/malfunction/file/**").permitAll()
                        .requestMatchers("/api/malfunction").hasAnyRole("CLIENT", "EMPLOYEE")
                        .requestMatchers("/api/malfunction/**").hasAnyRole("CLIENT", "EMPLOYEE")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutSuccessUrl("/api/logout"));

        http.cors(cors -> cors
                .configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                })
        );
        return http.build();
    }
}
