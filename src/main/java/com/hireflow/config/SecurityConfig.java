package com.hireflow.config;

import com.hireflow.security.JwtAuthenticationFilter;
import com.hireflow.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public Auth endpoints
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password",
                    "/api/v1/auth/verify-email",
                    "/api/v1/auth/resend-otp").permitAll()
                // Public Job endpoints
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/jobs",
                    "/api/v1/jobs/{id}",
                    "/api/v1/jobs/slug/**",
                    "/api/v1/skills",
                    "/api/v1/skills/categories").permitAll()
                // Swagger / API docs
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html").permitAll()
                // Actuator
                .requestMatchers("/actuator/**").permitAll()

                // SEEKER role
                .requestMatchers("/api/v1/seeker/**").hasRole("SEEKER")
                .requestMatchers(HttpMethod.POST, "/api/v1/applications").hasRole("SEEKER")
                .requestMatchers("/api/v1/applications/my-applications").hasRole("SEEKER")
                .requestMatchers("/api/v1/saved-jobs/**").hasRole("SEEKER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/applications/{id}").hasRole("SEEKER")

                // RECRUITER role
                .requestMatchers("/api/v1/recruiter/**").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/jobs/{id}").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/{id}").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/jobs/{id}/toggle").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.GET, "/api/v1/jobs/my-posts").hasRole("RECRUITER")
                .requestMatchers("/api/v1/interviews/**").hasAnyRole("RECRUITER", "SEEKER")

                // ADMIN role
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // All others require authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
