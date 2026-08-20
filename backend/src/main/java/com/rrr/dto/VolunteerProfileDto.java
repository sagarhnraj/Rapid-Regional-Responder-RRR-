package com.rrr.dto;

import java.util.UUID;

public class VolunteerProfileDto {
    private UUID userId;
    private Boolean isAvailable;
    private Integer maxRangeMeters;
    private String[] skills;
    private Double latitude;
    private Double longitude;

    public VolunteerProfileDto() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean available) { isAvailable = available; }

    public Integer getMaxRangeMeters() { return maxRangeMeters; }
    public void setMaxRangeMeters(Integer maxRangeMeters) { this.maxRangeMeters = maxRangeMeters; }

    public String[] getSkills() { return skills; }
    public void setSkills(String[] skills) { this.skills = skills; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
