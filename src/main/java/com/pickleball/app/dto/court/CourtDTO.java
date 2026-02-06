package com.pickleball.app.dto.court;

import com.pickleball.app.enums.CourtStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourtDTO {
    private Long courtId;
    private Long courtGroupId;
    private String courtName;
    private CourtStatus status;
    private BigDecimal basePricePerHour;
    // Additional fields for search/filter
    private String district;
    private String city;
    private String courtGroupName;
    // Additional fields from CourtGroup
    private String address; // Location/address from CourtGroup
    private String description; // Description from CourtGroup
    private String images; // Legacy images from CourtGroup (JSON string or comma-separated)
    // Image IDs for this court and its group
    private java.util.List<Long> courtImageIds;
    private java.util.List<Long> courtGroupImageIds;
    private String phone; // Phone from CourtGroup (if available) or manager phone
    // Amenities/Services - will be populated from Service entities
    private List<String> amenities; // List of service names available at this court group
    // Rating info (can be calculated from reviews if available)
    private Double rating; // Average rating
    private Integer reviewCount; // Number of reviews
}
