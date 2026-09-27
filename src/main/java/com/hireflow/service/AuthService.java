package com.hireflow.service;

import com.hireflow.dto.request.auth.LoginRequest;
import com.hireflow.dto.request.auth.RegisterRequest;
import com.hireflow.dto.response.auth.AuthResponse;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.entity.RecruiterProfile;
import com.hireflow.entity.Role;
import com.hireflow.entity.SeekerProfile;
import com.hireflow.entity.User;
import com.hireflow.event.UserRegisteredEvent;
import com.hireflow.exception.ApiException;
import com.hireflow.exception.DuplicateResourceException;
import com.hireflow.exception.ResourceNotFoundException;
import com.hireflow.repository.RecruiterProfileRepository;
import com.hireflow.repository.SeekerProfileRepository;
import com.hireflow.repository.UserRepository;
import com.hireflow.security.JwtUtil;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        Role role = Role.valueOf(request.getRole());
        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .build();

        user = userRepository.save(user);

        // Create role-specific profile
        if (role == Role.SEEKER) {
            SeekerProfile profile = SeekerProfile.builder().user(user).build();
            seekerProfileRepository.save(profile);
        } else if (role == Role.RECRUITER) {
            RecruiterProfile profile = RecruiterProfile.builder().user(user).build();
            recruiterProfileRepository.save(profile);
        }

        // Publish event for async email
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user));

        log.info("User registered: {} with role {}", user.getEmail(), role);
        return new MessageResponse("Registration successful! You can now log in.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (!user.getIsActive()) {
            throw new ApiException("Your account has been deactivated. Please contact support.", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(principal);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken("stripped") // Refresh token stripped
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
}
