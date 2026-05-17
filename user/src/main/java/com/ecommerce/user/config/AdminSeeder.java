package com.ecommerce.user.config;

import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps a single ADMIN account on startup so the catalog can be managed
 * out of the box. Credentials come from env; the user is created only if the
 * email is not already present.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository repo;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public AdminSeeder(UserRepository repo,
                       @Value("${admin.email:admin@ecommerce.local}") String adminEmail,
                       @Value("${admin.password:admin12345}") String adminPassword,
                       @Value("${admin.name:Administrator}") String adminName) {
        this.repo = repo;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(String... args) {
        if (repo.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setName(adminName);
        admin.setPasswordHash(new BCryptPasswordEncoder().encode(adminPassword));
        admin.setRole(Role.ADMIN);
        repo.save(admin);
    }
}
