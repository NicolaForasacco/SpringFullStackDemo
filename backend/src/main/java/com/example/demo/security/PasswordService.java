package com.example.demo.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final Argon2PasswordEncoder encoder;

    public PasswordService() {
        // Parametri Argon2id consigliati da OWASP:
        // saltLength: 16 bytes
        // hashLength: 32 bytes
        // parallelism: 1 thread
        // memory: 65536 KB (64 MB di RAM per hash)
        // iterations: 3 cicli
        this.encoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    public String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
