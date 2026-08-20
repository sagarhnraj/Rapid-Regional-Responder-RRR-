package com.rrr.service;

import com.rrr.dto.SOSCreateRequest;
import com.rrr.dto.SOSResponseDto;
import com.rrr.exception.BadRequestException;
import com.rrr.exception.ConflictException;
import com.rrr.exception.ResourceNotFoundException;
import com.rrr.model.*;
import com.rrr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SOSService {

    @Autowired
    private SOSRequestRepository sosRequestRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private VolunteerResponseRepository volunteerResponseRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IncidentHistoryRepository incidentHistoryRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${app.sos.system-max-radius-meters:1000}")
    private int systemMaxRadiusMeters;

    @Transactional
    public SOSResponseDto createSOS(UUID userId, SOSCreateRequest request) {
        // Create SOS Request
        SOSRequest sos = new SOSRequest();
        sos.setUserId(userId);
        sos.setType(request.getType());
        sos.setDescription(request.getDescription());
        sos.setLatitude(request.getLatitude());
        sos.setLongitude(request.getLongitude());
        sos.setAddress(request.getAddress());
        sos.setStatus("ACTIVE");

        sos = sosRequestRepository.save(sos);

        // Run Haversine Spatial Query (Max 1km = 1000m)
        List<Object[]> eligibleVolunteers = volunteerProfileRepository.findEligibleVolunteersWithinSystemRadius(
                request.getLatitude(),
                request.getLongitude(),
                systemMaxRadiusMeters
        );

        if (!eligibleVolunteers.isEmpty()) {
            sos.setStatus("NOTIFYING");
            sos = sosRequestRepository.save(sos);

            for (Object[] v : eligibleVolunteers) {
                UUID volunteerId = (UUID) v[0];
                String distanceText = v[6] + "m away";

                notificationService.createAndSendNotification(
                        volunteerId,
                        "🚨 " + request.getType() + " Emergency Alert (" + distanceText + ")",
                        "Emergency reported " + distanceText + ". Tap to view and respond.",
                        "SOS_ALERT",
                        sos.getId()
                );
            }
        }

        // Send confirmation to citizen
        notificationService.createAndSendNotification(
                userId,
                "SOS Emergency Alert Activated",
                "Your " + request.getType() + " alert is active. Locating nearby responders...",
                "SOS_STATUS",
                sos.getId()
        );

        // Record Incident History & Audit Log
        recordIncidentHistory(sos, null);
        auditLogRepository.save(new AuditLog(userId, "CREATE_SOS", "SOS ID: " + sos.getId() + " Type: " + sos.getType()));

        return mapToDto(sos);
    }

    @Transactional
    public SOSResponseDto acceptSOS(UUID sosId, UUID volunteerId, String estimatedArrival, String message) {
        // Atomic pessimistic write lock prevents race conditions
        SOSRequest sos = sosRequestRepository.findByIdForUpdate(sosId)
                .orElseThrow(() -> new ResourceNotFoundException("SOS request not found"));

        if ("RESOLVED".equals(sos.getStatus()) || "CANCELLED".equals(sos.getStatus())) {
            throw new BadRequestException("Cannot accept a resolved or cancelled emergency");
        }

        if ("ACCEPTED".equals(sos.getStatus()) || "RESPONDING".equals(sos.getStatus()) || "ARRIVED".equals(sos.getStatus())) {
            // Check if already accepted by another volunteer
            Optional<VolunteerResponse> existingResponse = volunteerResponseRepository.findBySosRequestIdAndVolunteerId(sosId, volunteerId);
            if (existingResponse.isEmpty()) {
                throw new ConflictException("This emergency has already been claimed by another responder");
            }
        }

        // Save Volunteer Response
        VolunteerResponse response = volunteerResponseRepository.findBySosRequestIdAndVolunteerId(sosId, volunteerId)
                .orElseGet(() -> {
                    VolunteerResponse r = new VolunteerResponse();
                    r.setSosRequestId(sosId);
                    r.setVolunteerId(volunteerId);
                    return r;
                });

        response.setResponseType("ACCEPTED");
        response.setEstimatedArrival(estimatedArrival != null ? estimatedArrival : "5-10 mins");
        response.setMessage(message != null ? message : "En route to emergency location");
        volunteerResponseRepository.save(response);

        // Update SOS Status
        sos.setStatus("ACCEPTED");
        sos = sosRequestRepository.save(sos);

        // Notify Citizen
        UserProfile volunteerProfile = userProfileRepository.findById(volunteerId).orElse(null);
        String volunteerName = volunteerProfile != null ? volunteerProfile.getName() : "A volunteer";

        notificationService.createAndSendNotification(
                sos.getUserId(),
                "Responder Assigned! 🚑",
                volunteerName + " accepted your emergency alert and is responding! ETA: " + response.getEstimatedArrival(),
                "SOS_ACCEPTED",
                sos.getId()
        );

        recordIncidentHistory(sos, volunteerId);
        auditLogRepository.save(new AuditLog(volunteerId, "ACCEPT_SOS", "Volunteer accepted SOS ID: " + sosId));

        return mapToDto(sos);
    }

    @Transactional
    public SOSResponseDto updateStatus(UUID sosId, String newStatus, UUID userId) {
        SOSRequest sos = sosRequestRepository.findByIdForUpdate(sosId)
                .orElseThrow(() -> new ResourceNotFoundException("SOS request not found"));

        String targetStatus = newStatus.toUpperCase().trim();
        validateStateTransition(sos.getStatus(), targetStatus);

        sos.setStatus(targetStatus);
        if ("RESOLVED".equals(targetStatus) || "CANCELLED".equals(targetStatus)) {
            sos.setResolvedAt(Instant.now());
        }

        sos = sosRequestRepository.save(sos);

        // Notify Citizen & Responders
        notificationService.createAndSendNotification(
                sos.getUserId(),
                "Emergency Status Update: " + targetStatus,
                "Your emergency status is now " + targetStatus,
                "SOS_STATUS",
                sos.getId()
        );

        recordIncidentHistory(sos, userId);
        auditLogRepository.save(new AuditLog(userId, "UPDATE_SOS_STATUS", "SOS ID: " + sosId + " Status: " + targetStatus));

        return mapToDto(sos);
    }

    public SOSResponseDto getSOSById(UUID sosId) {
        SOSRequest sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new ResourceNotFoundException("SOS request not found"));
        return mapToDto(sos);
    }

    public SOSResponseDto getActiveSOSForUser(UUID userId) {
        List<String> activeStatuses = Arrays.asList("ACTIVE", "NOTIFYING", "ACCEPTED", "RESPONDING", "ARRIVED");
        Optional<SOSRequest> sos = sosRequestRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(userId, activeStatuses);
        return sos.map(this::mapToDto).orElse(null);
    }

    public List<SOSResponseDto> getUserSOSHistory(UUID userId) {
        return sosRequestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SOSResponseDto> getActiveEmergencies() {
        List<String> activeStatuses = Arrays.asList("ACTIVE", "NOTIFYING", "ACCEPTED", "RESPONDING", "ARRIVED");
        return sosRequestRepository.findByStatusInOrderByCreatedAtDesc(activeStatuses)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void validateStateTransition(String currentStatus, String nextStatus) {
        if (currentStatus.equals(nextStatus)) return;

        // Disallow invalid transitions like RESOLVED -> ACTIVE
        if ("RESOLVED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new BadRequestException("Cannot modify an emergency that is already " + currentStatus);
        }
    }

    private void recordIncidentHistory(SOSRequest sos, UUID volunteerId) {
        IncidentHistory history = new IncidentHistory();
        history.setSosRequestId(sos.getId());
        history.setUserId(sos.getUserId());
        history.setVolunteerId(volunteerId);
        history.setType(sos.getType());
        history.setStatus(sos.getStatus());
        history.setLatitude(sos.getLatitude());
        history.setLongitude(sos.getLongitude());
        history.setAddress(sos.getAddress());
        history.setCreatedAt(sos.getCreatedAt());
        history.setResolvedAt(sos.getResolvedAt());

        incidentHistoryRepository.save(history);
    }

    public SOSResponseDto mapToDto(SOSRequest sos) {
        SOSResponseDto dto = new SOSResponseDto();
        dto.setId(sos.getId());
        dto.setUserId(sos.getUserId());

        UserProfile citizenProfile = userProfileRepository.findById(sos.getUserId()).orElse(null);
        if (citizenProfile != null) {
            dto.setUserName(citizenProfile.getName());
            dto.setUserPhone(citizenProfile.getPhone());
        }

        dto.setType(sos.getType());
        dto.setDescription(sos.getDescription());
        dto.setLatitude(sos.getLatitude());
        dto.setLongitude(sos.getLongitude());
        dto.setAddress(sos.getAddress());
        dto.setStatus(sos.getStatus());
        dto.setCreatedAt(sos.getCreatedAt());
        dto.setResolvedAt(sos.getResolvedAt());

        List<VolunteerResponse> responses = volunteerResponseRepository.findBySosRequestId(sos.getId());
        List<SOSResponseDto.VolunteerResponderDto> responderDtos = new ArrayList<>();

        for (VolunteerResponse vr : responses) {
            SOSResponseDto.VolunteerResponderDto rDto = new SOSResponseDto.VolunteerResponderDto();
            rDto.setVolunteerId(vr.getVolunteerId());
            rDto.setResponseType(vr.getResponseType());
            rDto.setEstimatedArrival(vr.getEstimatedArrival());
            rDto.setMessage(vr.getMessage());
            rDto.setCreatedAt(vr.getCreatedAt());

            UserProfile vProfile = userProfileRepository.findById(vr.getVolunteerId()).orElse(null);
            if (vProfile != null) {
                rDto.setVolunteerName(vProfile.getName());
                rDto.setVolunteerPhone(vProfile.getPhone());
            }

            responderDtos.add(rDto);
        }

        dto.setResponders(responderDtos);
        return dto;
    }
}
