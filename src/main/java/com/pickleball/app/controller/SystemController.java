package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    // In-memory settings for demo (in production, use database)
    private static final Map<String, String> settings = new HashMap<>();

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getSettings() {
        // Initialize default settings if empty
        if (settings.isEmpty()) {
            settings.put("siteName", "Pickleball Court Management");
            settings.put("maintenanceMode", "false");
            settings.put("maxBookingDays", "30");
            settings.put("cancellationHours", "24");
        }
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateSettings(@RequestBody Map<String, String> newSettings) {
        settings.putAll(newSettings);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}



