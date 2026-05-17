package com.ecommerce.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record Principal(String userId, String role) {
    }

    /**
     * Validates the token signature/expiry and returns its principal,
     * or {@code null} when the token is missing or invalid.
     */
    public Principal parse(String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(bearer.substring(7))
                    .getPayload();
            return new Principal(claims.getSubject(), String.valueOf(claims.get("role")));
        } catch (Exception ex) {
            return null;
        }
    }
}
