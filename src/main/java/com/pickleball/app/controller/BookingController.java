package com.pickleball.app.controller;

import com.pickleball.app.dto.booking.BookingRequest;
import com.pickleball.app.dto.booking.BookingResponse;
import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        User user = getCurrentUser();

        // Logic similar to frontend service mock
        // If query has userId, filter by it. If managerId, filter by it.
        // If user is basic customer, force userId = current.

        List<BookingResponse> bookings;
        if (user.getRole() == com.pickleball.app.enums.UserRole.CUSTOMER) {
            bookings = bookingService.getMyBookings(user);
        } else if (userId != null) {
            // Admin xem booking của một customer cụ thể
            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            bookings = bookingService.getBookingsByUser(targetUser);
        } else if (managerId != null) {
            bookings = bookingService.getBookingsByManager(user); // Logic inside service might need tweaks to respect
                                                                  // managerId param vs current user
        } else {
            bookings = bookingService.getBookingsByManager(user); // Admin/Manager view
        }

        // Apply filters in memory for now (or update service to accept criteria)
        // ...

        // Wrap in PageResponse structure expected by Frontend (BookingListResponse)
        // Frontend expects: { bookings: [], total: number, page: number, ... }
        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("total", bookings.size());
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("totalPages", 1);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        User user = getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(bookingService.createBooking(user, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelBooking(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = getCurrentUser();
        bookingService.cancelBooking(id, user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đã hủy đặt sân thành công");
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(@PathVariable Long id) {
        bookingService.checkIn(id);
        return ResponseEntity.ok(ApiResponse.success("Check-in successful"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        BookingResponse booking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookingCalendar(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long courtId,
            @RequestParam(required = false) Long managerId) {
        
        User currentUser = getCurrentUser();
        List<BookingResponse> bookings;
        
        if (currentUser.getRole() == com.pickleball.app.enums.UserRole.CUSTOMER) {
            bookings = bookingService.getMyBookings(currentUser);
        } else if (managerId != null) {
            bookings = bookingService.getBookingsByManager(
                userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found")));
        } else {
            bookings = bookingService.getBookingsByManager(currentUser);
        }
        
        // Filter by date range if provided
        // Filter by courtId if provided
        // Format as calendar events
        
        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
