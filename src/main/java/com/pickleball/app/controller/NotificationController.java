package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.notification.NotificationDTO;
import com.pickleball.app.dto.notification.NotificationListResponse;
import com.pickleball.app.entity.Notification;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.NotificationRepository;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .userId(notification.getUserId())
                .isRead(notification.isRead())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        User currentUser = getCurrentUser();
        Long targetUserId = userId != null ? userId : currentUser.getUserId();

        // Apply filters
        List<Notification> notifications = notificationService.getUserNotifications(targetUserId);
        
        if (type != null) {
            notifications = notifications.stream()
                    .filter(n -> type.equals(n.getType()))
                    .collect(Collectors.toList());
        }
        
        if (isRead != null) {
            notifications = notifications.stream()
                    .filter(n -> isRead.equals(n.isRead()))
                    .collect(Collectors.toList());
        }

        // Pagination
        int total = notifications.size();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<NotificationDTO> paginatedNotifications = notifications.stream()
                .skip(start)
                .limit(pageSize)
                .map(this::toDTO)
                .collect(Collectors.toList());

        NotificationListResponse response = NotificationListResponse.builder()
                .notifications(paginatedNotifications)
                .total((long) total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return ResponseEntity.ok(ApiResponse.success(toDTO(notification)));
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@RequestBody MarkAsReadRequest request) {
        notificationService.markAsRead(request.getNotificationId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PostMapping("/mark-all-as-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getUserId());
        notifications.forEach(n -> {
            if (!n.isRead()) {
                notificationService.markAsRead(n.getId());
            }
        });
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getUserId());
        long unreadCount = notifications.stream()
                .filter(n -> !n.isRead())
                .count();
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    // Inner class for request
    @lombok.Data
    static class MarkAsReadRequest {
        private Long notificationId;
    }
}

