package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.User;
import com.kmbank.exception.Exceptions;
import com.kmbank.repository.UserRepository;
import com.kmbank.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual constructor
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(UserResponse.fromEntity(userDetails.getUser())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        user = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", UserResponse.fromEntity(user)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new Exceptions.BadRequestException("Current password is incorrect");
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new Exceptions.BadRequestException("New password must be at least 8 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}
