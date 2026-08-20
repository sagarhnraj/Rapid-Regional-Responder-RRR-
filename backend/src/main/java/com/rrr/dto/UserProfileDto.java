package com.rrr.dto;

import java.util.UUID;

public class UserProfileDto {
    private UUID userId;
    private String name;
    private String phone;
    private String medicalInfo;

    public UserProfileDto() {}

    public UserProfileDto(UUID userId, String name, String phone, String medicalInfo) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.medicalInfo = medicalInfo;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMedicalInfo() { return medicalInfo; }
    public void setMedicalInfo(String medicalInfo) { this.medicalInfo = medicalInfo; }
}
