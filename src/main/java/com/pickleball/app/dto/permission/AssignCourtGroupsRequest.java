package com.pickleball.app.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignCourtGroupsRequest {
    private Long managerId;
    private List<Long> courtGroupIds;
}



