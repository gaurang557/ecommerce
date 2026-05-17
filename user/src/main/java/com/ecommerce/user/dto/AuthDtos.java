package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String password,
            @NotBlank String name) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password) {
    }

    public record UserView(Long id, String email, String name, String role) {
    }

    public record AuthResponse(String token, UserView user) {
    }
}
