package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.service.ServiceDTO;
import com.pickleball.app.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getServices(
            @RequestParam(required = false) Long courtGroupId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(serviceService.searchServices(courtGroupId, status, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(serviceService.getServiceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(serviceService.createService(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(serviceService.updateService(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted successfully"));
    }
}
