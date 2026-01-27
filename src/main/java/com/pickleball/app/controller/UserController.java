package com.pickleball.app.controller;

import com.pickleball.app.dto.auth.RegisterRequest;
import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.common.UserDTO;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Moved updateProfile to AuthController or keep here?
    // Frontend User service doesn't have updateProfile, AuthService does.
    // UserService has updateUser (Admin).

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsers(@RequestParam(required = false) String role) {
        List<UserDTO> users = userService.getAllUsers();
        if (role != null && !role.isEmpty()) {
            try {
                com.pickleball.app.enums.UserRole userRole = com.pickleball.app.enums.UserRole.valueOf(role.toUpperCase());
                users = users.stream()
                        .filter(u -> u.getRole() == userRole)
                        .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid role, return empty list or all users
                users = java.util.Collections.emptyList();
            }
        }
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody RegisterRequest request,
            @RequestParam String role) {
        return ResponseEntity.ok(ApiResponse.success(userService.createUser(request, role)));
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User blocked"));
    }

    @PutMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Long id) {
        userService.unblockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unblocked"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        Map<String, String> response = new HashMap<>();
        response.put("newPassword", newPassword);
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
