package com.rrr.service;

import com.rrr.dto.AuthResponse;
import com.rrr.dto.SOSResponseDto;
import com.rrr.dto.VolunteerLocationDto;
import com.rrr.dto.VolunteerOnboardRequest;
import com.rrr.dto.VolunteerProfileDto;
import com.rrr.exception.ResourceNotFoundException;
import com.rrr.model.User;
import com.rrr.model.UserProfile;
import com.rrr.model.VolunteerProfile;
import com.rrr.repository.UserProfileRepository;
import com.rrr.repository.UserRepository;
import com.rrr.repository.VolunteerProfileRepository;
import com.rrr.security.JwtTokenProvider;
import com.rrr.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VolunteerService {

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private SOSService sosService;

    public VolunteerProfileDto getVolunteerProfile(UUID userId) {
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer profile not found"));
        return mapToDto(profile);
    }

    @Transactional
    public AuthResponse onboardVolunteer(UUID userId, VolunteerOnboardRequest onboardDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Upgrade user role to VOLUNTEER
        user.setRole("VOLUNTEER");
        user = userRepository.save(user);

        // Create or activate VolunteerProfile
        VolunteerProfile profile = volunteerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    VolunteerProfile p = new VolunteerProfile();
                    p.setUserId(userId);
                    return p;
                });

        profile.setIsAvailable(true);
        if (onboardDto.getMaxRangeMeters() != null) profile.setMaxRangeMeters(onboardDto.getMaxRangeMeters());
        if (onboardDto.getSkills() != null) profile.setSkills(onboardDto.getSkills());
        if (onboardDto.getLatitude() != null) profile.setLatitude(onboardDto.getLatitude());
        if (onboardDto.getLongitude() != null) profile.setLongitude(onboardDto.getLongitude());

        volunteerProfileRepository.save(profile);

        // Issue updated JWT token reflecting VOLUNTEER authority
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        String jwt = tokenProvider.generateToken(authentication);

        UserProfile userProfile = userProfileRepository.findById(userId).orElse(null);
        String name = userProfile != null ? userProfile.getName() : user.getEmail();

        return new AuthResponse(jwt, user.getId(), user.getEmail(), name, user.getRole());
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
