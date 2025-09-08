package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.config.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                         PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    private ResponseEntity<Map<String, Object>> createResponse(HttpStatus status, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.value());
        response.put("message", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody User user) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                return createResponse(HttpStatus.BAD_REQUEST, "Email already exists", null);
            }
            if (userRepository.existsByUsername(user.getUsername())) {
                return createResponse(HttpStatus.BAD_REQUEST, "Username already exists", null);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // Ensure role is either USER or ADMIN
            if (!"USER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
                user.setRole("USER"); // Default to USER if invalid role
            }
            User savedUser = userRepository.save(user);
            String token = jwtUtil.generateToken(user.getEmail(), user.getUsername(), user.getRole());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", savedUser);
            responseData.put("token", token);

            return createResponse(HttpStatus.CREATED, "User registered successfully", responseData);
        } catch (Exception e) {
            return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user: " + e.getMessage(), null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User savedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();
            String token = jwtUtil.generateToken(user.getEmail(), savedUser.getUsername(), savedUser.getRole());
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("email", user.getEmail());
            responseData.put("username", savedUser.getUsername());
            responseData.put("role", savedUser.getRole());

            return createResponse(HttpStatus.OK, "Login successful", responseData);
        } catch (Exception e) {
            return createResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials: " + e.getMessage(), null);
        }
    }
}