package com.minimaya.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${minimaya.jwt.secret}")
    private String secret;

    @Value("${minimaya.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${minimaya.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String userId, String email) {
        return Jwts.builder()
            .subject(userId)
            .claims(Map.of("email", email, "type", "access"))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
            .signWith(signingKey())
            .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
            .subject(userId)
            .claims(Map.of("type", "refresh"))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
            .signWith(signingKey())
            .compact();
    }

    public Claims validateAndParse(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String extractUserId(String token) {
        return validateAndParse(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            validateAndParse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
