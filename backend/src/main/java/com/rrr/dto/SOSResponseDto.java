package com.rrr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SOSResponseDto {
    private UUID id;
    private UUID userId;
    private String userName;
    private String userPhone;
    private String type;
    private String description;
    private Double latitude;
    private Double longitude;
    private String address;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private List<VolunteerResponderDto> responders;

    public SOSResponseDto() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public List<VolunteerResponderDto> getResponders() { return responders; }
    public void setResponders(List<VolunteerResponderDto> responders) { this.responders = responders; }

    public static class VolunteerResponderDto {
        private UUID volunteerId;
        private String volunteerName;
        private String volunteerPhone;
        private String responseType;
        private String estimatedArrival;
        private String message;
        private Instant createdAt;

        public VolunteerResponderDto() {}

        public UUID getVolunteerId() { return volunteerId; }
        public void setVolunteerId(UUID volunteerId) { this.volunteerId = volunteerId; }

        public String getVolunteerName() { return volunteerName; }
        public void setVolunteerName(String volunteerName) { this.volunteerName = volunteerName; }

        public String getVolunteerPhone() { return volunteerPhone; }
        public void setVolunteerPhone(String volunteerPhone) { this.volunteerPhone = volunteerPhone; }

        public String getResponseType() { return responseType; }
        public void setResponseType(String responseType) { this.responseType = responseType; }

        public String getEstimatedArrival() { return estimatedArrival; }
        public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}
