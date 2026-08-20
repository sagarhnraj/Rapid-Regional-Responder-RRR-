package com.rrr.controller;

import com.rrr.dto.ApiResponse;
import com.rrr.dto.EmergencyContactDto;
import com.rrr.dto.UserProfileDto;
import com.rrr.security.UserPrincipal;
import com.rrr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citizens")
public class CitizenController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileDto profile = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody UserProfileDto dto) {
        UserProfileDto updated = userService.updateProfile(currentUser.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }

    @GetMapping("/contacts")
    public ResponseEntity<ApiResponse<List<EmergencyContactDto>>> getContacts(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<EmergencyContactDto> contacts = userService.getEmergencyContacts(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(contacts));
    }

    @PostMapping("/contacts")
    public ResponseEntity<ApiResponse<EmergencyContactDto>> addContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody EmergencyContactDto dto) {
        EmergencyContactDto created = userService.addEmergencyContact(currentUser.getId(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact added", created));
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") UUID contactId) {
        userService.deleteEmergencyContact(contactId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Emergency contact deleted", null));
    }
}
