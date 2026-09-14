package com.medicalstore.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private boolean active;
    private boolean accountVerified;
    private Set<String> roles;
}