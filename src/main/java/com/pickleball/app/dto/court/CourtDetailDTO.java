package com.pickleball.app.dto.court;

import com.pickleball.app.enums.CourtStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtDetailDTO {
    private Long courtId;
    private Long courtGroupId;
    private String courtName;
    private CourtStatus status;
    private BigDecimal basePricePerHour;
    private String courtGroupName;
    private String address;
    private String district;
    private String city;
    private String description;
    private String images;
    // Add reviews, ratings if needed
}



