package com.ecommerce.user.services;

import com.ecommerce.user.dto.AuthDtos.AuthResponse;
import com.ecommerce.user.dto.AuthDtos.LoginRequest;
import com.ecommerce.user.dto.AuthDtos.RegisterRequest;
import com.ecommerce.user.dto.AuthDtos.UserView;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repositories.UserRepository;
import com.ecommerce.user.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final JwtService jwtService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo, JwtService jwtService) {
        this.repo = repo;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already registered");
        }
        User user = new User();
        user.setEmail(req.email());
        user.setName(req.name());
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(Role.CUSTOMER);
        repo.save(user);
        return new AuthResponse(jwtService.generate(user), toView(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = repo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return new AuthResponse(jwtService.generate(user), toView(user));
    }

    public UserView getById(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toView(user);
    }

    private UserView toView(User u) {
        return new UserView(u.getId(), u.getEmail(), u.getName(), u.getRole().name());
    }
}
