package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.TimeSlotLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller để quản lý temporary lock cho time slots
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/time-slots")
@RequiredArgsConstructor
public class TimeSlotLockController {

    private final TimeSlotLockService timeSlotLockService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Reserve (lock) một time slot
     * POST /api/v1/time-slots/{slotId}/reserve
     */
    @PostMapping("/{slotId}/reserve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reserveSlot(
            @PathVariable Long slotId,
            @RequestParam(required = false, defaultValue = "5") Integer minutes) {
        
        User user = getCurrentUser();
        Duration duration = Duration.ofMinutes(minutes);
        
        try {
            timeSlotLockService.lockSlot(slotId, user, duration);
            return ResponseEntity.ok(ApiResponse.success(
                "Đã giữ khung giờ thành công. Bạn có " + minutes + " phút để hoàn tất đặt sân.",
                Map.of("slotId", slotId, "expiresInMinutes", minutes)
            ));
        } catch (Exception e) {
            log.error("Error reserving slot {}: {}", slotId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    /**
     * Reserve (lock) nhiều time slots
     * POST /api/v1/time-slots/reserve
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> reserveSlots(
            @RequestBody ReserveSlotsRequest request) {
        
        User user = getCurrentUser();
        Duration duration = Duration.ofMinutes(request.getMinutes() != null ? request.getMinutes() : 5);
        
        try {
            timeSlotLockService.lockSlots(request.getSlotIds(), user, duration);
            return ResponseEntity.ok(ApiResponse.success(
                "Đã giữ " + request.getSlotIds().size() + " khung giờ thành công. Bạn có " + 
                (request.getMinutes() != null ? request.getMinutes() : 5) + " phút để hoàn tất đặt sân.",
                Map.of("slotIds", request.getSlotIds(), "expiresInMinutes", request.getMinutes() != null ? request.getMinutes() : 5)
            ));
        } catch (Exception e) {
            log.error("Error reserving slots: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    /**
     * Release lock của một slot
     * DELETE /api/v1/time-slots/{slotId}/reserve
     */
    @DeleteMapping("/{slotId}/reserve")
    public ResponseEntity<ApiResponse<Void>> releaseSlot(
            @PathVariable Long slotId) {
        
        User user = getCurrentUser();
        
        // Kiểm tra user có quyền release lock không (chỉ user đang giữ lock mới được release)
        if (timeSlotLockService.isSlotLockedByOtherUser(slotId, user.getUserId())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Bạn không có quyền giải phóng lock này", 400));
        }
        
        timeSlotLockService.releaseLock(slotId);
        return ResponseEntity.ok(ApiResponse.success("Đã giải phóng khung giờ"));
    }

    /**
     * Gia hạn lock của một slot
     * PUT /api/v1/time-slots/{slotId}/reserve/extend
     */
    @PutMapping("/{slotId}/reserve/extend")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extendReservation(
            @PathVariable Long slotId,
            @RequestParam(required = false, defaultValue = "5") Integer minutes) {
        
        User user = getCurrentUser();
        Duration duration = Duration.ofMinutes(minutes);
        
        try {
            timeSlotLockService.extendLock(slotId, user, duration);
            return ResponseEntity.ok(ApiResponse.success(
                "Đã gia hạn thời gian giữ khung giờ thêm " + minutes + " phút",
                Map.of("slotId", slotId, "additionalMinutes", minutes)
            ));
        } catch (Exception e) {
            log.error("Error extending reservation for slot {}: {}", slotId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), 400));
        }
    }

    /**
     * Kiểm tra slot có đang bị lock không
     * GET /api/v1/time-slots/{slotId}/reserve/status
     */
    @GetMapping("/{slotId}/reserve/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlotLockStatus(@PathVariable Long slotId) {
        Long lockedByUserId = timeSlotLockService.getLockedByUserId(slotId);
        boolean isLocked = lockedByUserId != null;
        Map<String, Object> response = new HashMap<>();
        response.put("slotId", slotId);
        response.put("isLocked", isLocked);
        response.put("lockedByUserId", lockedByUserId);
        
        User currentUser = getCurrentUser();
        response.put("isLockedByCurrentUser", isLocked && lockedByUserId != null && lockedByUserId.equals(currentUser.getUserId()));
        
        return ResponseEntity.ok(ApiResponse.success(
            isLocked ? "Khung giờ đang được giữ" : "Khung giờ còn trống",
            response
        ));
    }

    /**
     * Batch check lock status của nhiều slots
     * POST /api/v1/time-slots/lock-status
     */
    @PostMapping("/lock-status")
    public ResponseEntity<ApiResponse<Map<Long, Map<String, Object>>>> getSlotsLockStatus(
            @RequestBody List<Long> slotIds) {
        
        User currentUser = getCurrentUser();
        Map<Long, Long> lockInfo = timeSlotLockService.getLockedByUserIds(slotIds);
        
        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Long slotId : slotIds) {
            Long lockedByUserId = lockInfo.get(slotId);
            boolean isLocked = lockedByUserId != null;
            
            Map<String, Object> status = new HashMap<>();
            status.put("slotId", slotId);
            status.put("isLocked", isLocked);
            status.put("lockedByUserId", lockedByUserId);
            status.put("isLockedByCurrentUser", isLocked && lockedByUserId != null && lockedByUserId.equals(currentUser.getUserId()));
            
            result.put(slotId, status);
        }
        
        return ResponseEntity.ok(ApiResponse.success("Lock status retrieved", result));
    }

    /**
     * DTO cho request reserve nhiều slots
     */
    public static class ReserveSlotsRequest {
        private List<Long> slotIds;
        private Integer minutes;

        public List<Long> getSlotIds() {
            return slotIds;
        }

        public void setSlotIds(List<Long> slotIds) {
            this.slotIds = slotIds;
        }

        public Integer getMinutes() {
            return minutes;
        }

        public void setMinutes(Integer minutes) {
            this.minutes = minutes;
        }
    }
}

