package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.security.CustomUserDetails;
import com.kmbank.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DashboardResponse dashboard = dashboardService.getDashboard(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
