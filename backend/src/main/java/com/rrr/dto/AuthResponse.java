package com.rrr.dto;

import java.util.UUID;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UUID userId;
    private String email;
    private String name;
    private String role;

    public AuthResponse(String token, UUID userId, String email, String name, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
}
