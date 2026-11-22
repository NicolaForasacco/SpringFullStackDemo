package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FullstackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FullstackApplication.class, args);
    }

    /**
     * Inizializza il database con un utente di default e prodotti di prova
     */
    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          PasswordService passwordService) {
        return args -> {
            // Crea utente admin/demo se non esiste
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setFullName("Amministratore");
                // Password cifrata con Argon2id: "Admin123!"
                admin.setPassword(passwordService.hashPassword("Admin123!"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println(">>> Creato utente di test: admin@example.com / Admin123!");
            }

            // Inserisce prodotti iniziali se la tabella è vuota
            if (productRepository.count() == 0) {
                productRepository.save(new Product("Laptop Pro 16", 1899.99, "Computer portatile ad alte prestazioni"));
                productRepository.save(new Product("Mouse Wireless Ergonomico", 49.50, "Mouse ottico ricaricabile USB-C"));
                productRepository.save(new Product("Tastiera Meccanica RGB", 129.00, "Switch lineari silenziosi"));
                productRepository.save(new Product("Monitor 27 pollici 4K", 420.00, "Pannello IPS HDR400"));
                System.out.println(">>> Inseriti 4 prodotti di default nel database.");
            }
        };
    }
}
