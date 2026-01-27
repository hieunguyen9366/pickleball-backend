package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.timeslot.TimeSlotConfigDTO;
import com.pickleball.app.dto.timeslot.TimeSlotConfigRequest;
import com.pickleball.app.service.TimeSlotConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/time-slots/configs")
@RequiredArgsConstructor
public class TimeSlotConfigController {

    private final TimeSlotConfigService configService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<List<TimeSlotConfigDTO>>> getConfigs(
            @RequestParam(required = false) Long courtId,
            @RequestParam(required = false) Long courtGroupId
    ) {
        List<TimeSlotConfigDTO> configs = configService.getConfigs(courtId, courtGroupId);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotConfigDTO>> getConfigById(@PathVariable Long id) {
        TimeSlotConfigDTO config = configService.getConfigById(id);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotConfigDTO>> createConfig(
            @Valid @RequestBody TimeSlotConfigRequest request
    ) {
        TimeSlotConfigDTO config = configService.createConfig(request);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotConfigDTO>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotConfigRequest request
    ) {
        TimeSlotConfigDTO config = configService.updateConfig(id, request);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/court/{courtId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COURT_MANAGER')")
    public ResponseEntity<ApiResponse<TimeSlotConfigDTO>> getConfigForCourt(@PathVariable Long courtId) {
        TimeSlotConfigDTO config = configService.getConfigForCourt(courtId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }
}

