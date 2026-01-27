package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.court.CourtGroupDTO;
import com.pickleball.app.dto.permission.AssignCourtGroupsRequest;
import com.pickleball.app.dto.permission.ManagerCourtGroupsResponse;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.CourtGroupRepository;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final CourtService courtService;
    private final CourtGroupRepository courtGroupRepository;
    private final UserRepository userRepository;

    private CourtGroupDTO toDTO(CourtGroup group) {
        return CourtGroupDTO.builder()
                .courtGroupId(group.getCourtGroupId())
                .groupName(group.getGroupName())
                .address(group.getAddress())
                .district(group.getDistrict())
                .city(group.getCity())
                .description(group.getDescription())
                .images(group.getImages())
                .managerId(group.getManager() != null ? group.getManager().getUserId() : null)
                .build();
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ManagerCourtGroupsResponse>> getManagerCourtGroups(
            @PathVariable Long managerId) {

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Get assigned court groups
        List<CourtGroupDTO> assignedGroups = courtService.getCourtGroupsByManager(managerId);

        // Get all court groups
        List<CourtGroupDTO> allGroups = courtService.getAllCourtGroups();

        // Get available (unassigned) court groups
        List<Long> assignedIds = assignedGroups.stream()
                .map(CourtGroupDTO::getCourtGroupId)
                .collect(Collectors.toList());

        List<CourtGroupDTO> availableGroups = allGroups.stream()
                .filter(g -> !assignedIds.contains(g.getCourtGroupId()))
                .collect(Collectors.toList());

        ManagerCourtGroupsResponse response = ManagerCourtGroupsResponse.builder()
                .managerId(managerId)
                .managerName(manager.getFullName())
                .assignedCourtGroups(assignedGroups)
                .availableCourtGroups(availableGroups)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignCourtGroups(
            @RequestBody AssignCourtGroupsRequest request) {

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        for (Long courtGroupId : request.getCourtGroupIds()) {
            CourtGroup group = courtGroupRepository.findById(courtGroupId)
                    .orElseThrow(() -> new RuntimeException("Court Group not found"));
            group.setManager(manager);
            courtGroupRepository.save(group);
        }

        return ResponseEntity.ok(ApiResponse.success("Court groups assigned successfully"));
    }

    @DeleteMapping("/unassign/{managerId}/{courtGroupId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unassignCourtGroup(
            @PathVariable Long managerId,
            @PathVariable Long courtGroupId) {

        CourtGroup group = courtGroupRepository.findById(courtGroupId)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        if (group.getManager() == null || !group.getManager().getUserId().equals(managerId)) {
            throw new RuntimeException("Court group is not assigned to this manager");
        }

        group.setManager(null);
        courtGroupRepository.save(group);

        return ResponseEntity.ok(ApiResponse.success("Court group unassigned successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ManagerCourtGroupsResponse>>> getAllAssignments() {
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.pickleball.app.enums.UserRole.COURT_MANAGER)
                .collect(Collectors.toList());

        List<ManagerCourtGroupsResponse> responses = managers.stream()
                .map(manager -> {
                    List<CourtGroupDTO> assignedGroups = courtService.getCourtGroupsByManager(manager.getUserId());
                    List<CourtGroupDTO> allGroups = courtService.getAllCourtGroups();
                    List<Long> assignedIds = assignedGroups.stream()
                            .map(CourtGroupDTO::getCourtGroupId)
                            .collect(Collectors.toList());
                    List<CourtGroupDTO> availableGroups = allGroups.stream()
                            .filter(g -> !assignedIds.contains(g.getCourtGroupId()))
                            .collect(Collectors.toList());

                    return ManagerCourtGroupsResponse.builder()
                            .managerId(manager.getUserId())
                            .managerName(manager.getFullName())
                            .assignedCourtGroups(assignedGroups)
                            .availableCourtGroups(availableGroups)
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}



