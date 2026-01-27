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
    private String images;
}
