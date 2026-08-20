package com.rrr.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;

    private String phone;

    public GoogleAuthRequest() {}

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
