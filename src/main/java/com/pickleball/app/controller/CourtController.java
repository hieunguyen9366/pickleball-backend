package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.court.CourtDTO;
import com.pickleball.app.dto.court.CourtGroupDTO;
import com.pickleball.app.dto.court.CourtGroupRequest;
import com.pickleball.app.dto.court.CourtRequest;
import com.pickleball.app.dto.court.ImageDTO;
import com.pickleball.app.service.CourtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // --- Court Groups ---

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<CourtGroupDTO>>> getCourtGroups(
            @RequestParam(required = false) Long managerId) {
        if (managerId != null) {
            return ResponseEntity.ok(ApiResponse.success(courtService.getCourtGroupsByManager(managerId)));
        }
        return ResponseEntity.ok(ApiResponse.success(courtService.getAllCourtGroups()));
    }

    @GetMapping("/groups/{id}")
    public ResponseEntity<ApiResponse<CourtGroupDTO>> getCourtGroupById(@PathVariable Long id) {
        // Implement getById in service if missing, or use existing
        // For now finding from all (inefficient but works for fix)
        // Ideally add getById to service
        return ResponseEntity.ok(ApiResponse.success(courtService.getAllCourtGroups().stream()
                .filter(g -> g.getCourtGroupId().equals(id)).findFirst().orElse(null)));
    }

    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CourtGroupDTO>> createCourtGroup(@Valid @RequestBody CourtGroupRequest request) {
        // Convert Request DTO to DTO for service
        CourtGroupDTO dto = CourtGroupDTO.builder()
                .groupName(request.getGroupName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .city(request.getCity())
                .description(request.getDescription())
                .images(request.getImages())
                .managerId(request.getManagerId())
                .build();
        return ResponseEntity.ok(ApiResponse.success(courtService.createCourtGroup(dto)));
    }

    @PutMapping("/groups/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<CourtGroupDTO>> updateCourtGroup(@PathVariable Long id,
            @Valid @RequestBody CourtGroupRequest request) {
        // Convert Request DTO to DTO for service
        CourtGroupDTO dto = CourtGroupDTO.builder()
                .groupName(request.getGroupName())
                .address(request.getAddress())
                .district(request.getDistrict())
                .city(request.getCity())
                .description(request.getDescription())
                .images(request.getImages())
                .managerId(request.getManagerId())
                .build();
        return ResponseEntity.ok(ApiResponse.success(courtService.updateCourtGroup(id, dto)));
    }

    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourtGroup(@PathVariable Long id) {
        courtService.deleteCourtGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Court Group deleted successfully"));
    }

    // --- Court Group Images ---

    @PostMapping("/groups/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<java.util.List<Long>>> uploadCourtGroupImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(courtService.uploadCourtGroupImage(id, file)));
    }

    @GetMapping("/groups/{id}/images")
    public ResponseEntity<ApiResponse<java.util.List<ImageDTO>>> getCourtGroupImages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCourtGroupImages(id)));
    }

    @GetMapping("/groups/images/{imageId}")
    public ResponseEntity<ApiResponse<ImageDTO>> getCourtGroupImageById(@PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCourtGroupImageById(imageId)));
    }

    // --- Courts ---

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> searchCourts(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long courtGroupId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        // Use search endpoint logic
        // If it's a simple list request from admin (no search params often), handle it
        // Integrating searchCourts from service
        return ResponseEntity.ok(ApiResponse.success(courtService.searchCourts(date, startTime, null)));
    }

    @GetMapping("/search") // Frontend calls /search explicitly in searchCourts()
    public ResponseEntity<ApiResponse<com.pickleball.app.dto.court.CourtSearchResponse>> searchCourtsExplicit(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long courtGroupId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) java.util.List<String> amenities,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        
        // Build search request DTO
        com.pickleball.app.dto.court.CourtSearchRequestDTO request = 
            com.pickleball.app.dto.court.CourtSearchRequestDTO.builder()
                .searchTerm(searchTerm)
                .district(district)
                .city(city)
                .courtGroupId(courtGroupId)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(status != null ? com.pickleball.app.enums.CourtStatus.valueOf(status) : null)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .amenities(amenities)
                .sortBy(sortBy)
                .page(page)
                .pageSize(pageSize)
                .build();
        
        com.pickleball.app.dto.court.CourtSearchResponse response = 
            courtService.searchCourtsAdvanced(request);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/districts")
    public ResponseEntity<ApiResponse<List<String>>> getDistricts() {
        return ResponseEntity.ok(ApiResponse.success(courtService.getDistricts()));
    }
    
    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCities()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<com.pickleball.app.dto.court.CourtDetailDTO>> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCourtDetailById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<CourtDTO>> createCourt(@Valid @RequestBody CourtRequest request) {
        // Convert Request DTO to DTO for service
        CourtDTO dto = new CourtDTO();
        dto.setCourtGroupId(request.getCourtGroupId());
        dto.setCourtName(request.getCourtName());
        dto.setStatus(request.getStatus());
        dto.setBasePricePerHour(request.getBasePricePerHour());
        return ResponseEntity.ok(ApiResponse.success(courtService.createCourt(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<CourtDTO>> updateCourt(@PathVariable Long id, @Valid @RequestBody CourtRequest request) {
        // Convert Request DTO to DTO for service
        CourtDTO dto = new CourtDTO();
        dto.setCourtGroupId(request.getCourtGroupId());
        dto.setCourtName(request.getCourtName());
        dto.setStatus(request.getStatus());
        dto.setBasePricePerHour(request.getBasePricePerHour());
        return ResponseEntity.ok(ApiResponse.success(courtService.updateCourt(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.ok(ApiResponse.success("Court deleted successfully"));
    }

    // --- Court Images ---

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<java.util.List<Long>>> uploadCourtImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(courtService.uploadCourtImage(id, file)));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<java.util.List<ImageDTO>>> getCourtImages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCourtImages(id)));
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<ImageDTO>> getCourtImageById(@PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getCourtImageById(imageId)));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<com.pickleball.app.dto.court.TimeSlotDTO>>> getCourtTimeSlots(
            @PathVariable Long id,
            @RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success(courtService.getAvailableTimeSlots(id, date)));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<com.pickleball.app.dto.court.AvailabilityResponse>> checkAvailability(
            @PathVariable Long id,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        boolean available = courtService.checkTimeSlotAvailability(id, date, startTime, endTime);
        com.pickleball.app.dto.court.AvailabilityResponse response = 
            com.pickleball.app.dto.court.AvailabilityResponse.builder()
                .available(available)
                .message(available ? "Time slot is available" : "Time slot is not available")
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
