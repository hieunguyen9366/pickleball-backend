package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtGroupDTO {
    private Long courtGroupId;
    private Long managerId;
    private String groupName;
    private String address;
    private String district;
    private String city;
    private String description;
    /**
     * Legacy images field (e.g. URLs/JSON) - kept for backward compatibility.
     */
    private String images;

    /**
     * IDs of images stored in court_group_images table.
     */
    private java.util.List<Long> imageIds;
}
