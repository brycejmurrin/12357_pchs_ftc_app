package com.minimaya.api.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    int expiresIn,
    UserSummary user
) {
    public record UserSummary(String id, String email, String displayName) {}
}
