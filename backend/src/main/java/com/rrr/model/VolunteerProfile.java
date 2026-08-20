package com.rrr.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "volunteer_profiles")
public class VolunteerProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "max_range_meters")
    private Integer maxRangeMeters = 1000;

    @Column(name = "skills", columnDefinition = "text[]")
    private String[] skills = new String[0];

    @Column(nullable = false)
    private Double latitude = 0.0;

    @Column(nullable = false)
    private Double longitude = 0.0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public VolunteerProfile() {}

    public VolunteerProfile(User user) {
        this.user = user;
        this.userId = user.getId();
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
