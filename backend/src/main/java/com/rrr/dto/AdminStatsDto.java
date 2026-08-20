package com.rrr.dto;

import java.util.Map;

public class AdminStatsDto {
    private long totalUsers;
    private long totalCitizens;
    private long totalVolunteers;
    private long availableVolunteers;
    private long totalSOSRequests;
    private long activeSOSRequests;
    private long resolvedSOSRequests;
    private long cancelledSOSRequests;
    private long totalResponses;
    private double averageResponseTimeMinutes;
    private Map<String, Long> emergencyTypeDistribution;

    public AdminStatsDto() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalCitizens() { return totalCitizens; }
    public void setTotalCitizens(long totalCitizens) { this.totalCitizens = totalCitizens; }

    public long getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(long totalVolunteers) { this.totalVolunteers = totalVolunteers; }

    public long getAvailableVolunteers() { return availableVolunteers; }
    public void setAvailableVolunteers(long availableVolunteers) { this.availableVolunteers = availableVolunteers; }

    public long getTotalSOSRequests() { return totalSOSRequests; }
    public void setTotalSOSRequests(long totalSOSRequests) { this.totalSOSRequests = totalSOSRequests; }

    public long getActiveSOSRequests() { return activeSOSRequests; }
    public void setActiveSOSRequests(long activeSOSRequests) { this.activeSOSRequests = activeSOSRequests; }

    public long getResolvedSOSRequests() { return resolvedSOSRequests; }
    public void setResolvedSOSRequests(long resolvedSOSRequests) { this.resolvedSOSRequests = resolvedSOSRequests; }

    public long getCancelledSOSRequests() { return cancelledSOSRequests; }
    public void setCancelledSOSRequests(long cancelledSOSRequests) { this.cancelledSOSRequests = cancelledSOSRequests; }

    public long getTotalResponses() { return totalResponses; }
    public void setTotalResponses(long totalResponses) { this.totalResponses = totalResponses; }

    public double getAverageResponseTimeMinutes() { return averageResponseTimeMinutes; }
    public void setAverageResponseTimeMinutes(double averageResponseTimeMinutes) { this.averageResponseTimeMinutes = averageResponseTimeMinutes; }

    public Map<String, Long> getEmergencyTypeDistribution() { return emergencyTypeDistribution; }
    public void setEmergencyTypeDistribution(Map<String, Long> emergencyTypeDistribution) { this.emergencyTypeDistribution = emergencyTypeDistribution; }
}
