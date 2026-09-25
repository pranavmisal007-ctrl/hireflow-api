package com.hireflow.security;

import com.hireflow.config.JwtConfig;
import com.hireflow.entity.Role;
import com.hireflow.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET =
        "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = Mockito.mock(JwtConfig.class);
        when(jwtConfig.getSecret()).thenReturn(SECRET);
        when(jwtConfig.getAccessTokenExpiryMs()).thenReturn(900000L);
        jwtUtil = new JwtUtil(jwtConfig);
    }

    private UserPrincipal makePrincipal() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.SEEKER);
        return UserPrincipal.from(user);
    }

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateAccessToken(makePrincipal());
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void extractEmailFromToken() {
        String token = jwtUtil.generateAccessToken(makePrincipal());
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    void tamperInvalidatesToken() {
        String token = jwtUtil.generateAccessToken(makePrincipal());
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() throws InterruptedException {
        JwtConfig shortConfig = Mockito.mock(JwtConfig.class);
        when(shortConfig.getSecret()).thenReturn(SECRET);
        when(shortConfig.getAccessTokenExpiryMs()).thenReturn(1L); // 1ms
        JwtUtil shortLived = new JwtUtil(shortConfig);

        String token = shortLived.generateAccessToken(makePrincipal());
        Thread.sleep(20);
        assertThat(shortLived.validateToken(token)).isFalse();
    }
}
