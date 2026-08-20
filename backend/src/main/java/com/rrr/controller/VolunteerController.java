package com.rrr.controller;

import com.rrr.dto.ApiResponse;
import com.rrr.dto.AuthResponse;
import com.rrr.dto.SOSResponseDto;
import com.rrr.dto.VolunteerLocationDto;
import com.rrr.dto.VolunteerOnboardRequest;
import com.rrr.dto.VolunteerProfileDto;
import com.rrr.security.UserPrincipal;
import com.rrr.service.VolunteerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volunteers")
public class VolunteerController {

    @Autowired
    private VolunteerService volunteerService;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<AuthResponse>> onboardVolunteer(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody VolunteerOnboardRequest onboardDto) {
        AuthResponse response = volunteerService.onboardVolunteer(currentUser.getId(), onboardDto);
        return ResponseEntity.ok(ApiResponse.ok("Successfully onboarded as Volunteer", response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<VolunteerProfileDto>> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        VolunteerProfileDto profile = volunteerService.getVolunteerProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<VolunteerProfileDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody VolunteerProfileDto dto) {
        VolunteerProfileDto updated = volunteerService.updateProfile(currentUser.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Volunteer settings updated", updated));
    }

    @PatchMapping("/availability")
    public ResponseEntity<ApiResponse<VolunteerProfileDto>> toggleAvailability(@AuthenticationPrincipal UserPrincipal currentUser) {
        VolunteerProfileDto updated = volunteerService.toggleAvailability(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Availability updated", updated));
    }

    @PatchMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody VolunteerLocationDto locationDto) {
        volunteerService.updateLocation(currentUser.getId(), locationDto);
        return ResponseEntity.ok(ApiResponse.ok("Location updated", null));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<SOSResponseDto>>> getNearbyEmergencies() {
        List<SOSResponseDto> nearby = volunteerService.getNearbyEmergencies();
        return ResponseEntity.ok(ApiResponse.ok(nearby));
    }
}
