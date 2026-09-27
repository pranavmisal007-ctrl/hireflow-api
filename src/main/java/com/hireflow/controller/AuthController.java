package com.hireflow.controller;

import com.hireflow.dto.request.auth.LoginRequest;
import com.hireflow.dto.request.auth.RegisterRequest;
import com.hireflow.dto.response.auth.AuthResponse;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.security.UserPrincipal;
import com.hireflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth endpoints: register, login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (SEEKER or RECRUITER)")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login → returns JWT access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of(
            "id", principal.getId(),
            "email", principal.getEmail(),
            "role", principal.getRole()
        ));
    }
}
