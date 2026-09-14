package com.medicalstore.user.controller;

import com.medicalstore.user.dto.RegisterRequest;
import com.medicalstore.user.dto.UserResponse;
import com.medicalstore.user.entity.User;
import com.medicalstore.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.medicalstore.otp.dto.OtpVerifyRequest;
import com.medicalstore.otp.entity.OtpVerification;
import com.medicalstore.otp.repository.OtpVerificationRepository;
import com.medicalstore.otp.service.OtpService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;
    private final OtpVerificationRepository otpVerificationRepository;

    public UserController(
            UserService userService,
            OtpService otpService,
            OtpVerificationRepository otpVerificationRepository) {

        this.userService = userService;
        this.otpService = otpService;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        otpService.generateOtp(
                user.getPhone(),
                "REGISTRATION"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. OTP sent for account verification.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<UserResponse> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {

        OtpVerification otpVerification =
                otpVerificationRepository
                        .findTopByPhoneAndPurposeOrderByCreatedAtDesc(
                                request.getPhone(),
                                "REGISTRATION"
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No OTP found for this phone number"
                                ));

        if (otpVerification.isVerified()) {
            throw new RuntimeException("OTP has already been verified");
        }

        if (otpVerification.getExpiresAt()
                .isBefore(java.time.LocalDateTime.now())) {

            throw new RuntimeException("OTP has expired");
        }

        if (otpVerification.getAttempts() >= 5) {
            throw new RuntimeException(
                    "Maximum OTP attempts exceeded"
            );
        }

        if (!otpVerification.getOtp().equals(request.getOtp())) {

            otpVerification.setAttempts(
                    otpVerification.getAttempts() + 1
            );

            otpVerificationRepository.save(otpVerification);

            throw new RuntimeException("Invalid OTP");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        User user = userService.verifyAccount(request.getPhone());

        return ResponseEntity.ok(
                userService.convertToResponse(user)
        );
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(
                userService.getAllUsers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserResponse> getUserByPhone(
            @PathVariable String phone) {

        return ResponseEntity.ok(
                userService.getUserByPhone(phone)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                userService.updateUserStatus(id, active)
        );
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {

        return ResponseEntity.ok(
                userService.existsByEmail(email)
        );
    }

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {

        return ResponseEntity.ok(
                userService.existsByPhone(phone)
        );
    }
}