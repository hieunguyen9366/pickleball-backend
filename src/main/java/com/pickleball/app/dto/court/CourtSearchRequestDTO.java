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
public class CourtSearchRequestDTO {
    // Search term
    private String searchTerm; // Search in courtName, courtGroupName, address, district, city
    
    // Location filters
    private String district;
    private String city;
    private Long courtGroupId;
    
    // Time filters
    private String date; // Format: YYYY-MM-DD
    private String startTime; // Format: HH:mm
    private String endTime; // Format: HH:mm
    
    // Price filters
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    // Rating filter
    private Double minRating;
    
    // Amenities filter
    private List<String> amenities; // List of service names
    
    // Status filter
    private CourtStatus status;
    
    // Sorting
    private String sortBy; // default, price_asc, price_desc, rating_desc, name_asc
    
    // Pagination
    private Integer page;
    private Integer pageSize;
}

