package com.medicalstore.otp.repository;

import com.medicalstore.otp.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification>
    findTopByPhoneAndPurposeOrderByCreatedAtDesc(
            String phone,
            String purpose
    );
}