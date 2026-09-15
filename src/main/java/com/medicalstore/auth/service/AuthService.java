package com.medicalstore.auth.service;

import com.medicalstore.auth.dto.LoginRequest;
import com.medicalstore.auth.dto.LoginResponse;
import com.medicalstore.security.JwtService;
import com.medicalstore.user.entity.User;
import com.medicalstore.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.medicalstore.otp.entity.OtpVerification;
import com.medicalstore.otp.repository.OtpVerificationRepository;
import com.medicalstore.otp.service.OtpService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final OtpVerificationRepository otpVerificationRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            OtpService otpService,
            OtpVerificationRepository otpVerificationRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!user.isAccountVerified()) {
            throw new RuntimeException("Account is not verified");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        var roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    public void sendLoginOtp(String phone) {

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!user.isAccountVerified()) {
            throw new RuntimeException("Account is not verified");
        }

        otpService.generateOtp(phone, "LOGIN");
    }

    public LoginResponse loginWithOtp(
            String phone,
            String otp) {

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive");
        }

        if (!user.isAccountVerified()) {
            throw new RuntimeException("Account is not verified");
        }

        OtpVerification otpVerification =
                otpVerificationRepository
                        .findTopByPhoneAndPurposeOrderByCreatedAtDesc(
                                phone,
                                "LOGIN"
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

        if (!otpVerification.getOtp().equals(otp)) {

            otpVerification.setAttempts(
                    otpVerification.getAttempts() + 1
            );

            otpVerificationRepository.save(otpVerification);

            throw new RuntimeException("Invalid OTP");
        }

        otpVerification.setVerified(true);
        otpVerificationRepository.save(otpVerification);

        String token = jwtService.generateToken(user.getEmail());

        var roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(java.util.stream.Collectors.toSet());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}