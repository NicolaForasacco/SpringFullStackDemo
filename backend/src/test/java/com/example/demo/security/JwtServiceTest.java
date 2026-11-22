package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "chiave_segreta_molto_lunga_per_firmare_i_jwt_con_hs256_almeno_256bit";
    private final long expirationMs = 3600000; // 1 ora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Dovrebbe generare un token ed estrarre subject, role e fullName con successo")
    void testGenerateAndExtractClaims() {
        String email = "mario.rossi@example.com";
        String role = "ADMIN";
        String fullName = "Mario Rossi";

        String token = jwtService.generateToken(email, role, fullName);

        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(role, jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    @DisplayName("Dovrebbe invalidare il token se l'utente non corrisponde al subject")
    void testTokenInvalidForDifferentUsername() {
        String token = jwtService.generateToken("user1@example.com", "USER", "User One");

        assertFalse(jwtService.isTokenValid(token, "user2@example.com"));
    }

    @Test
    @DisplayName("Dovrebbe rifiutare un token scaduto")
    void testExpiredToken() {
        // Token con durata 0 millisecondi
        JwtService expiredJwtService = new JwtService(secret, -1000);
        String token = expiredJwtService.generateToken("test@example.com", "USER", "Test User");

        assertThrows(Exception.class, () -> expiredJwtService.extractUsername(token));
    }
}
