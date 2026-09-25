package com.hireflow.service;

import com.hireflow.dto.request.auth.*;
import com.hireflow.dto.response.auth.AuthResponse;
import com.hireflow.dto.response.auth.MessageResponse;
import com.hireflow.entity.*;
import com.hireflow.event.UserRegisteredEvent;
import com.hireflow.exception.*;
import com.hireflow.repository.*;
import com.hireflow.security.JwtUtil;
import com.hireflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SeekerProfileRepository seekerProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
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

        // Generate OTP and save
        String otp = generateOtp();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
            .token(otp)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();
        emailVerificationTokenRepository.save(verificationToken);

        // Publish event for async email
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, otp));

        log.info("User registered: {} with role {}", user.getEmail(), role);
        return new MessageResponse("Registration successful! Please check your email for the OTP verification code.");
    }

    @Transactional
    public MessageResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getIsEmailVerified()) {
            return new MessageResponse("Email already verified.");
        }

        EmailVerificationToken token = emailVerificationTokenRepository
            .findByTokenAndUser(request.getOtp(), user)
            .orElseThrow(() -> new InvalidTokenException("Invalid OTP code"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("OTP has expired. Please request a new one.");
        }

        user.setIsEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.deleteByUser(user);

        // Send welcome email asynchronously (inline here since it's a follow-up to registration)
        log.info("Email verified for user: {}", user.getEmail());
        return new MessageResponse("Email verified successfully! You can now log in.");
    }

    @Transactional
    public MessageResponse resendOtp(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.getIsEmailVerified()) {
            return new MessageResponse("Email is already verified.");
        }

        emailVerificationTokenRepository.deleteByUser(user);

        String otp = generateOtp();
        EmailVerificationToken token = EmailVerificationToken.builder()
            .token(otp)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();
        emailVerificationTokenRepository.save(token);

        eventPublisher.publishEvent(new UserRegisteredEvent(this, user, otp));
        return new MessageResponse("New OTP sent to " + email);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        if (!user.getIsEmailVerified()) {
            throw new ApiException("Please verify your email before logging in.", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsActive()) {
            throw new ApiException("Your account has been deactivated. Please contact support.", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(principal);
        RefreshToken refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .isEmailVerified(user.getIsEmailVerified())
            .build();
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.getIsRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        // Rotate: revoke old, issue new
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        UserPrincipal principal = UserPrincipal.from(user);
        String newAccessToken = jwtUtil.generateAccessToken(principal);
        RefreshToken newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .isEmailVerified(user.getIsEmailVerified())
            .build();
    }

    @Transactional
    public MessageResponse logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);
        });
        return new MessageResponse("Logged out successfully.");
    }

    @Transactional
    public MessageResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
            .orElse(null);

        // Don't reveal if user doesn't exist (security best practice)
        if (user == null) {
            return new MessageResponse("If an account with that email exists, a password reset link has been sent.");
        }

        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashSHA256(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(hashedToken)
            .user(user)
            .expiryDate(LocalDateTime.now().plusMinutes(30))
            .build();
        passwordResetTokenRepository.save(resetToken);

        // Use the raw token in the email (user submits it back, we hash and compare)
        // For simplicity here, we send the raw token. In production, hash on receipt.
        // TODO: Wire up emailService here (avoid circular dependency if needed)
        log.info("Password reset token generated for {}: {}", email, rawToken);

        return new MessageResponse("If an account with that email exists, a password reset link has been sent.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        String hashedToken = hashSHA256(request.getToken());

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(hashedToken)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUser(user);
        passwordResetTokenRepository.deleteByUser(user);

        return new MessageResponse("Password reset successfully. Please log in with your new password.");
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiryDate(LocalDateTime.now().plusDays(7))
            .build();
        return refreshTokenRepository.save(token);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
