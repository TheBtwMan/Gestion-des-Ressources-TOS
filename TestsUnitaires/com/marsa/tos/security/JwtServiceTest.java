package com.marsa.tos.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires purs (sans Spring) pour JwtService.
 * Utilise un secret de 32+ caractères et une durée d'expiration longue pour les tests standards.
 */
class JwtServiceTest {

    private JwtService jwtService;

    // Secret de test (doit faire au moins 32 octets / 256 bits pour HMAC-SHA256)
    private static final String TEST_SECRET = "MarSaMaRoC2026SecretKeyForJWTTests!";
    private static final long EXPIRATION_MS = 3600000L; // 1 heure

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    void givenUserDetails_whenGenerateToken_thenTokenIsNotEmpty() {
        // Given
        UserDetails userDetails = User.builder()
                .username("ADMIN001")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PARAMETRAGE")))
                .build();

        // When
        String token = jwtService.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT format: header.payload.signature
    }

    @Test
    void givenValidToken_whenExtractUsername_thenReturnsCorrectUsername() {
        // Given
        UserDetails userDetails = User.builder()
                .username("USER999")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CONSULTATION")))
                .build();
        String token = jwtService.generateToken(userDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("USER999");
    }

    @Test
    void givenValidTokenAndMatchingUser_whenIsTokenValid_thenReturnsTrue() {
        // Given
        UserDetails userDetails = User.builder()
                .username("ADMIN001")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_GESTION_UTILISATEURS")))
                .build();
        String token = jwtService.generateToken(userDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void givenValidTokenAndDifferentUser_whenIsTokenValid_thenReturnsFalse() {
        // Given
        UserDetails admin = User.builder()
                .username("ADMIN001")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_GESTION_UTILISATEURS")))
                .build();
        UserDetails otherUser = User.builder()
                .username("OTHER_USER")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CONSULTATION")))
                .build();
        String token = jwtService.generateToken(admin);

        // When
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void givenExpiredToken_whenIsTokenValid_thenThrowsExpiredJwtException() {
        // Given — Create a JwtService with 0ms expiration (token expires immediately)
        JwtService expiredService = new JwtService(TEST_SECRET, 0L);
        UserDetails userDetails = User.builder()
                .username("ADMIN001")
                .password("hashed-pwd")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PARAMETRAGE")))
                .build();
        String token = expiredService.generateToken(userDetails);

        // When & Then — extracting claims from an expired token throws ExpiredJwtException
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void givenUserWithMultipleAuthorities_whenGenerateAndExtract_thenUsernameIsPreserved() {
        // Given
        UserDetails userDetails = User.builder()
                .username("MULTI_ROLE_USER")
                .password("hashed-pwd")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_PARAMETRAGE"),
                        new SimpleGrantedAuthority("ROLE_AFFECTATION_PREVISIONNELLE"),
                        new SimpleGrantedAuthority("ROLE_CONSULTATION")
                ))
                .build();

        // When
        String token = jwtService.generateToken(userDetails);
        String extracted = jwtService.extractUsername(token);

        // Then
        assertThat(extracted).isEqualTo("MULTI_ROLE_USER");
    }
}
