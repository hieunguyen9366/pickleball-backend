package com.pickleball.app.dto.court;

import com.pickleball.app.enums.CourtStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtRequest {
    
    @NotNull(message = "Court Group ID is required")
    private Long courtGroupId;
    
    @NotBlank(message = "Court name is required")
    @Size(max = 50, message = "Court name must not exceed 50 characters")
    private String courtName;
    
    @NotNull(message = "Status is required")
    private CourtStatus status;
    
    @NotNull(message = "Base price per hour is required")
    @Positive(message = "Base price per hour must be positive")
    private BigDecimal basePricePerHour;
}

