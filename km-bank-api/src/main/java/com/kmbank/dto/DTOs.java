package com.kmbank.dto;

import com.kmbank.entity.Account;
import com.kmbank.entity.Transaction;
import com.kmbank.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DTOs {

    // ============ AUTH DTOs ============
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Email is Required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is Required")
        @Email(message = "Invalid  email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String type;
        private UserResponse user;
    }

    // ======== USER DTOs ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phoneNumber;
        private String profileImage;
        private String role;
        private Boolean enabled;
        private LocalDateTime createdAt;

        public static UserResponse fromEntity(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .profileImage(user.getProfileImage())
                    .role(user.getRole().name())
                    .enabled(user.getEnabled())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String profileImage;
    }

}
