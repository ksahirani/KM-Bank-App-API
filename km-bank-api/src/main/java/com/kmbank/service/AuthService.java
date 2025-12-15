package com.kmbank.service;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.Account;
import com.kmbank.entity.User;
import com.kmbank.exception.Exceptions;
import com.kmbank.repository.AccountRepository;
import com.kmbank.repository.UserRepository;
import com.kmbank.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new Exceptions.DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();
        user = userRepository.save(user);

        // Create default checking account for new user
        Account defaultAccount = Account.builder()
                .accountName("Primary Checking")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(1000.00)) // Welcome bonus
                .user(user)
                .build();

        accountRepository.save(defaultAccount);

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
