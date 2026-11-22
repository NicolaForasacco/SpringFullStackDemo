package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("Dovrebbe cifrare la password generando un hash Argon2id valido")
    void testHashPasswordGeneratesArgon2idHash() {
        String rawPassword = "SecretPassword123!";
        String hash = passwordService.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$argon2id$"), "L'hash deve iniziare con il prefisso $argon2id$");
    }

    @Test
    @DisplayName("Dovrebbe verificare con successo la password corretta")
    void testMatchesWithCorrectPassword() {
        String rawPassword = "CorrectPassword123!";
        String hash = passwordService.hashPassword(rawPassword);

        assertTrue(passwordService.matches(rawPassword, hash));
    }

    @Test
    @DisplayName("Dovrebbe rifiutare una password errata")
    void testMatchesWithWrongPassword() {
        String rawPassword = "CorrectPassword123!";
        String hash = passwordService.hashPassword(rawPassword);

        assertFalse(passwordService.matches("WrongPassword!", hash));
    }

    @Test
    @DisplayName("Due hash della stessa password devono essere diversi grazie al salt casuale")
    void testSaltsDifferForSamePassword() {
        String rawPassword = "PasswordTest!";
        String hash1 = passwordService.hashPassword(rawPassword);
        String hash2 = passwordService.hashPassword(rawPassword);

        assertNotEquals(hash1, hash2, "I due hash devono differire per effetto del salt crittografico");
        assertTrue(passwordService.matches(rawPassword, hash1));
        assertTrue(passwordService.matches(rawPassword, hash2));
    }
}
