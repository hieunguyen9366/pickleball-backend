package com.pickleball.app.dto.permission;

import com.pickleball.app.dto.court.CourtGroupDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerCourtGroupsResponse {
    private Long managerId;
    private String managerName;
    private List<CourtGroupDTO> assignedCourtGroups;
    private List<CourtGroupDTO> availableCourtGroups;
}



