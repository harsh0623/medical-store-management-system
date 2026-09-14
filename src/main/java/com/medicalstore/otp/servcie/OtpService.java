package com.medicalstore.otp.service;

import com.medicalstore.otp.entity.OtpVerification;
import com.medicalstore.otp.repository.OtpVerificationRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;

    public OtpService(OtpVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    public OtpVerification generateOtp(String phone, String purpose) {

        int otpNumber = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNumber);

        OtpVerification otpVerification = OtpVerification.builder()
                .phone(phone)
                .otp(otp)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .attempts(0)
                .build();

        OtpVerification savedOtp = otpRepository.save(otpVerification);

        // Development only - later replaced with SMS/WhatsApp provider
        System.out.println(
                "OTP for " + phone + " (" + purpose + "): " + otp
        );

        return savedOtp;
    }
}