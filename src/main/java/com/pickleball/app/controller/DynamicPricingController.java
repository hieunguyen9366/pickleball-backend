package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtPriceConfig;
import com.pickleball.app.repository.CourtPriceConfigRepository;
import com.pickleball.app.repository.CourtRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class DynamicPricingController {
    
    // Note: This controller shares the same base path as CourtController
    // The endpoints are differentiated by the full path pattern

    private final CourtPriceConfigRepository priceConfigRepository;
    private final CourtRepository courtRepository;

    @GetMapping("/{courtId}/pricing")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<List<DynamicPricingDTO>>> getPricingConfigs(@PathVariable Long courtId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        
        List<DynamicPricingDTO> configs = priceConfigRepository.findByCourt(court).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PostMapping("/{courtId}/pricing")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<DynamicPricingDTO>> createPricingConfig(
            @PathVariable Long courtId,
            @RequestBody CreatePricingRequest request) {
        
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));
        
        CourtPriceConfig config = CourtPriceConfig.builder()
                .court(court)
                .timeStart(LocalTime.parse(request.getTimeStart()))
                .timeEnd(LocalTime.parse(request.getTimeEnd()))
                .daysOfWeek(request.getDaysOfWeek())
                .priceModifier(request.getPriceModifier())
                .isHoliday(request.isHoliday())
                .build();
        
        CourtPriceConfig saved = priceConfigRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDTO(saved)));
    }

    @PutMapping("/{courtId}/pricing/{configId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<DynamicPricingDTO>> updatePricingConfig(
            @PathVariable Long courtId,
            @PathVariable Long configId,
            @RequestBody CreatePricingRequest request) {
        
        CourtPriceConfig config = priceConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Pricing config not found"));
        
        if (!config.getCourt().getCourtId().equals(courtId)) {
            throw new RuntimeException("Pricing config does not belong to this court");
        }
        
        config.setTimeStart(LocalTime.parse(request.getTimeStart()));
        config.setTimeEnd(LocalTime.parse(request.getTimeEnd()));
        config.setDaysOfWeek(request.getDaysOfWeek());
        config.setPriceModifier(request.getPriceModifier());
        config.setHoliday(request.isHoliday());
        
        CourtPriceConfig saved = priceConfigRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDTO(saved)));
    }

    @DeleteMapping("/{courtId}/pricing/{configId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deletePricingConfig(
            @PathVariable Long courtId,
            @PathVariable Long configId) {
        
        CourtPriceConfig config = priceConfigRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Pricing config not found"));
        
        if (!config.getCourt().getCourtId().equals(courtId)) {
            throw new RuntimeException("Pricing config does not belong to this court");
        }
        
        priceConfigRepository.delete(config);
        return ResponseEntity.ok(ApiResponse.success("Pricing config deleted successfully"));
    }

    private DynamicPricingDTO toDTO(CourtPriceConfig config) {
        return DynamicPricingDTO.builder()
                .configId(config.getConfigId())
                .courtId(config.getCourt().getCourtId())
                .timeStart(config.getTimeStart().toString())
                .timeEnd(config.getTimeEnd().toString())
                .daysOfWeek(config.getDaysOfWeek())
                .priceModifier(config.getPriceModifier())
                .isHoliday(config.isHoliday())
                .build();
    }

    @Data
    @lombok.Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    static class DynamicPricingDTO {
        private Long configId;
        private Long courtId;
        private String timeStart;
        private String timeEnd;
        private String daysOfWeek;
        private BigDecimal priceModifier;
        private boolean isHoliday;
    }

    @Data
    static class CreatePricingRequest {
        private String timeStart;
        private String timeEnd;
        private String daysOfWeek;
        private BigDecimal priceModifier;
        private boolean isHoliday;
    }
}

