package com.rrr.service;

import com.rrr.dto.AdminStatsDto;
import com.rrr.model.SOSRequest;
import com.rrr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private SOSRequestRepository sosRequestRepository;

    @Autowired
    private VolunteerResponseRepository volunteerResponseRepository;

    public AdminStatsDto getSystemStatistics() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCitizens(userRepository.countByRole("CITIZEN"));
        stats.setTotalVolunteers(userRepository.countByRole("VOLUNTEER"));
        stats.setAvailableVolunteers(volunteerProfileRepository.countByIsAvailableTrue());

        stats.setTotalSOSRequests(sosRequestRepository.count());
        stats.setActiveSOSRequests(sosRequestRepository.countByStatus("ACTIVE") + sosRequestRepository.countByStatus("NOTIFYING"));
        stats.setResolvedSOSRequests(sosRequestRepository.countByStatus("RESOLVED"));
        stats.setCancelledSOSRequests(sosRequestRepository.countByStatus("CANCELLED"));
        stats.setTotalResponses(volunteerResponseRepository.count());

        List<SOSRequest> allSos = sosRequestRepository.findAll();
        Map<String, Long> distribution = new HashMap<>();

        long totalDurationMinutes = 0;
        long resolvedCount = 0;

        for (SOSRequest sos : allSos) {
            distribution.put(sos.getType(), distribution.getOrDefault(sos.getType(), 0L) + 1);

            if (sos.getResolvedAt() != null && sos.getCreatedAt() != null) {
                long minutes = Duration.between(sos.getCreatedAt(), sos.getResolvedAt()).toMinutes();
                totalDurationMinutes += minutes;
                resolvedCount++;
            }
        }

        stats.setEmergencyTypeDistribution(distribution);
        stats.setAverageResponseTimeMinutes(resolvedCount > 0 ? (double) totalDurationMinutes / resolvedCount : 0.0);

        return stats;
    }
}
