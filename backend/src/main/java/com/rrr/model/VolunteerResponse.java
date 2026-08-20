package com.rrr.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "volunteer_responses", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sos_request_id", "volunteer_id"})
})
public class VolunteerResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sos_request_id", nullable = false)
    private UUID sosRequestId;

    @Column(name = "volunteer_id", nullable = false)
    private UUID volunteerId;

    @Column(name = "response_type", nullable = false)
    private String responseType; // ACCEPTED, DECLINED

    @Column(name = "estimated_arrival")
    private String estimatedArrival = "";

    @Column(columnDefinition = "TEXT")
    private String message = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public VolunteerResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSosRequestId() { return sosRequestId; }
    public void setSosRequestId(UUID sosRequestId) { this.sosRequestId = sosRequestId; }

    public UUID getVolunteerId() { return volunteerId; }
    public void setVolunteerId(UUID volunteerId) { this.volunteerId = volunteerId; }

    public String getResponseType() { return responseType; }
    public void setResponseType(String responseType) { this.responseType = responseType; }

    public String getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(String estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
