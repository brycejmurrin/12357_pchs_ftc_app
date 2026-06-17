package com.minimaya.api.controller;

import com.minimaya.api.dto.AuthResponse;
import com.minimaya.api.dto.LoginRequest;
import com.minimaya.api.dto.RegisterRequest;
import com.minimaya.domain.model.AppUser;
import com.minimaya.domain.repository.AppUserRepository;
import com.minimaya.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        final AppUser user = userRepository.save(AppUser.builder()
            .email(req.email())
            .displayName(req.displayName())
            .passwordHash(passwordEncoder.encode(req.password()))
            .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        final AppUser user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ResponseEntity.ok(buildAuthResponse(user));
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        final String accessToken  = jwtService.generateAccessToken(
            user.getId().toString(), user.getEmail());
        final String refreshToken = jwtService.generateRefreshToken(user.getId().toString());
        return new AuthResponse(accessToken, refreshToken, 900,
            new AuthResponse.UserSummary(
                user.getId().toString(), user.getEmail(), user.getDisplayName()));
    }
}
