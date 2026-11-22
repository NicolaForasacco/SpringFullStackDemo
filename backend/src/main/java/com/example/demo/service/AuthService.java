package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.PasswordService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordService passwordService,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email già registrata nel sistema");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        // Cifratura della password con Argon2id
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.USER);

        userRepository.save(user);

        // Genera il token JWT
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getFullName());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));

        // Verifica la password con l'hash Argon2id memorizzato
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenziali non valide");
        }

        // Genera il token JWT
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name(), user.getFullName());
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole().name());
    }
}
