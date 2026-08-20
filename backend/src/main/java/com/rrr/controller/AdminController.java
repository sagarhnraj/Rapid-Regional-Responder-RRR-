package com.rrr.controller;

import com.rrr.dto.AdminStatsDto;
import com.rrr.dto.ApiResponse;
import com.rrr.dto.SOSResponseDto;
import com.rrr.service.AdminService;
import com.rrr.service.SOSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SOSService sosService;

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminStatsDto>> getSystemReports() {
        AdminStatsDto stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/sos")
    public ResponseEntity<ApiResponse<List<SOSResponseDto>>> getAllActiveEmergencies() {
        List<SOSResponseDto> emergencies = sosService.getActiveEmergencies();
        return ResponseEntity.ok(ApiResponse.ok(emergencies));
    }
}
