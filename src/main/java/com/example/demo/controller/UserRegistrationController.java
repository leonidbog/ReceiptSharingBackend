package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.service.UserRegistrationService;
import com.example.demo.controller.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;
    private final PasswordEncoder passwordEncoder;
    private UserDetailsService userDetailsService;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        try {
            userRegistrationService.registerUser(userDTO);
            return ResponseEntity.ok("User registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());
            if (!passwordEncoder.matches(userDTO.getPassword(), userDetails.getPassword())){
                return ResponseEntity.badRequest().body("Incorrect username or password");
            }
            return ResponseEntity.ok("User logged successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}