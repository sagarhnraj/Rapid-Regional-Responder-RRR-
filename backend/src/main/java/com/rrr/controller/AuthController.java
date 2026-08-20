package com.rrr.controller;

import com.rrr.dto.ApiResponse;
import com.rrr.dto.AuthRequest;
import com.rrr.dto.AuthResponse;
import com.rrr.dto.GoogleAuthRequest;
import com.rrr.dto.RegisterRequest;
import com.rrr.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/verify-google-registration")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyGoogleRegistration(@Valid @RequestBody GoogleAuthRequest request) {
        Map<String, String> googleIdentity = authService.verifyGoogleRegistrationIdentity(request);
        return ResponseEntity.ok(ApiResponse.ok("Google identity verified for registration", googleIdentity));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("User registered successfully as CITIZEN", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
