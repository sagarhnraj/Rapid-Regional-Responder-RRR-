package com.rrr.controller;

import com.rrr.dto.ApiResponse;
import com.rrr.dto.SOSCreateRequest;
import com.rrr.dto.SOSResponseDto;
import com.rrr.security.UserPrincipal;
import com.rrr.service.SOSService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sos")
public class SOSController {

    @Autowired
    private SOSService sosService;

    @PostMapping
    public ResponseEntity<ApiResponse<SOSResponseDto>> createSOS(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SOSCreateRequest request) {
        SOSResponseDto response = sosService.createSOS(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Emergency SOS alert broadcasted", response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SOSResponseDto>> getActiveSOS(@AuthenticationPrincipal UserPrincipal currentUser) {
        SOSResponseDto active = sosService.getActiveSOSForUser(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(active));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<SOSResponseDto>>> getMySOSHistory(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<SOSResponseDto> history = sosService.getUserSOSHistory(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SOSResponseDto>> getSOSById(@PathVariable("id") UUID id) {
        SOSResponseDto sos = sosService.getSOSById(id);
        return ResponseEntity.ok(ApiResponse.ok(sos));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<SOSResponseDto>> acceptSOS(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String eta = body != null ? body.get("estimatedArrival") : "5-10 mins";
        String message = body != null ? body.get("message") : "En route";

        SOSResponseDto response = sosService.acceptSOS(id, currentUser.getId(), eta, message);
        return ResponseEntity.ok(ApiResponse.ok("Emergency accepted", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SOSResponseDto>> updateStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") UUID id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        SOSResponseDto updated = sosService.updateStatus(id, status, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Status updated to " + status, updated));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SOSResponseDto>> cancelSOS(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") UUID id) {
        SOSResponseDto cancelled = sosService.updateStatus(id, "CANCELLED", currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Emergency alert cancelled", cancelled));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<SOSResponseDto>> resolveSOS(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("id") UUID id) {
        SOSResponseDto resolved = sosService.updateStatus(id, "RESOLVED", currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok("Emergency marked resolved", resolved));
    }
}
