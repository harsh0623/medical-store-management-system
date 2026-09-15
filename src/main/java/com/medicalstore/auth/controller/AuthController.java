package com.medicalstore.auth.controller;

import com.medicalstore.auth.dto.LoginRequest;
import com.medicalstore.auth.dto.LoginResponse;
import com.medicalstore.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.medicalstore.auth.dto.OtpLoginRequest;
import com.medicalstore.auth.dto.SendOtpRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<String> sendLoginOtp(
            @Valid @RequestBody SendOtpRequest request) {

        authService.sendLoginOtp(request.getPhone());

        return ResponseEntity.ok(
                "OTP sent successfully"
        );
    }

    @PostMapping("/login-otp")
    public ResponseEntity<LoginResponse> loginWithOtp(
            @Valid @RequestBody OtpLoginRequest request) {

        return ResponseEntity.ok(
                authService.loginWithOtp(
                        request.getPhone(),
                        request.getOtp()
                )
        );
    }
}