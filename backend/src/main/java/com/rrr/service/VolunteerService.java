package com.rrr.service;

import com.rrr.dto.SOSResponseDto;
import com.rrr.dto.VolunteerLocationDto;
import com.rrr.dto.VolunteerProfileDto;
import com.rrr.exception.ResourceNotFoundException;
import com.rrr.model.VolunteerProfile;
import com.rrr.repository.VolunteerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VolunteerService {

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private SOSService sosService;

    public VolunteerProfileDto getVolunteerProfile(UUID userId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));
        return mapToDto(profile);
    }

    @Transactional
    public VolunteerProfileDto updateProfile(UUID userId, VolunteerProfileDto dto) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    VolunteerProfile p = new VolunteerProfile();
                    p.setUserId(userId);
                    return p;
                });

        if (dto.getIsAvailable() != null) profile.setIsAvailable(dto.getIsAvailable());
        if (dto.getMaxRangeMeters() != null) profile.setMaxRangeMeters(dto.getMaxRangeMeters());
        if (dto.getSkills() != null) profile.setSkills(dto.getSkills());

        profile = volunteerProfileRepository.save(profile);
        return mapToDto(profile);
    }

    @Transactional
    public VolunteerProfileDto toggleAvailability(UUID userId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    VolunteerProfile p = new VolunteerProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setIsAvailable(!profile.getIsAvailable());
        profile = volunteerProfileRepository.save(profile);
        return mapToDto(profile);
    }

    @Transactional
    public void updateLocation(UUID userId, VolunteerLocationDto locationDto) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    VolunteerProfile p = new VolunteerProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setLatitude(locationDto.getLatitude());
        profile.setLongitude(locationDto.getLongitude());
        volunteerProfileRepository.save(profile);
    }

    public List<SOSResponseDto> getNearbyEmergencies() {
        return sosService.getActiveEmergencies();
    }

    private VolunteerProfileDto mapToDto(VolunteerProfile p) {
        VolunteerProfileDto dto = new VolunteerProfileDto();
        dto.setUserId(p.getUserId());
        dto.setIsAvailable(p.getIsAvailable());
        dto.setMaxRangeMeters(p.getMaxRangeMeters());
        dto.setSkills(p.getSkills());
        dto.setLatitude(p.getLatitude());
        dto.setLongitude(p.getLongitude());
        return dto;
    }
}
