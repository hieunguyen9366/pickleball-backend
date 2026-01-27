package com.pickleball.app.service.impl;

import com.pickleball.app.dto.booking.BookingRequest;
import com.pickleball.app.dto.booking.BookingResponse;
import com.pickleball.app.dto.booking.BookingServiceRequest;
import com.pickleball.app.dto.booking.BookingServiceResponse;
import com.pickleball.app.entity.*;
import com.pickleball.app.enums.BookingStatus;
import com.pickleball.app.enums.PaymentStatus;
import com.pickleball.app.enums.UserRole;
import com.pickleball.app.repository.BookingRepository;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.repository.ServiceRepository;
import com.pickleball.app.repository.TimeSlotRepository;
import com.pickleball.app.service.BookingService;
import com.pickleball.app.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotService timeSlotService;
    private final com.pickleball.app.service.EmailService emailService;
    private final com.pickleball.app.service.NotificationService notificationService;
    private final com.pickleball.app.service.TimeSlotLockService timeSlotLockService;

    @Override
    @Transactional
    public BookingResponse createBooking(User user, BookingRequest request) {
        log.info("Creating booking for user {}: courtId={}, date={}, time={}-{}",
                user.getUserId(), request.getCourtId(), request.getBookingDate(),
                request.getStartTime(), request.getEndTime());

        Court court = courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> {
                    log.error("Court not found: {}", request.getCourtId());
                    return new RuntimeException("Court not found");
                });

        // Validate time
        if (request.getStartTime().isAfter(request.getEndTime())) {
            log.error("Invalid time range: startTime {} is after endTime {}",
                    request.getStartTime(), request.getEndTime());
            throw new RuntimeException("Start time must be before end time");
        }

        // Check availability từ TimeSlot table với PESSIMISTIC LOCK để tránh race
        // condition
        // Lock các rows để đảm bảo chỉ 1 transaction có thể check và update cùng lúc
        List<com.pickleball.app.entity.TimeSlot> overlappingSlots = timeSlotRepository.findOverlappingSlotsWithLock(
                court.getCourtId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime());

        if (overlappingSlots.isEmpty()) {
            log.warn("No time slots found for courtId={}, date={}, time={}-{}",
                    court.getCourtId(), request.getBookingDate(),
                    request.getStartTime(), request.getEndTime());
            throw new RuntimeException("Không tìm thấy khung giờ cho khoảng thời gian này. Vui lòng chọn lại.");
        }

        // Kiểm tra tất cả slots trong khoảng thời gian phải available và không bị lock
        // bởi user khác
        List<Long> slotIds = overlappingSlots.stream()
                .map(com.pickleball.app.entity.TimeSlot::getSlotId)
                .collect(Collectors.toList());

        // Kiểm tra lock - nếu slot bị lock bởi user khác, không cho phép đặt
        if (timeSlotLockService != null) {
            for (Long slotId : slotIds) {
                if (timeSlotLockService.isSlotLockedByOtherUser(slotId, user.getUserId())) {
                    log.warn("Slot {} is locked by another user", slotId);
                    throw new RuntimeException("Một số khung giờ đang được người khác giữ. Vui lòng chọn lại.");
                }
            }
        }

        // Kiểm tra tất cả slots trong khoảng thời gian phải available
        boolean allSlotsAvailable = overlappingSlots.stream()
                .allMatch(slot -> slot.getIsAvailable() && slot.getBooking() == null);

        if (!allSlotsAvailable) {
            log.warn("Some slots are not available for courtId={}, date={}, time={}-{}",
                    court.getCourtId(), request.getBookingDate(),
                    request.getStartTime(), request.getEndTime());
            throw new RuntimeException("Một số khung giờ đã được đặt. Vui lòng chọn lại.");
        }

        // Tính giá từ các TimeSlot (mỗi slot có giá riêng)
        BigDecimal totalPrice = overlappingSlots.stream()
                .map(slot -> slot.getPrice() != null ? slot.getPrice() : court.getBasePricePerHour())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCourt(court);
        booking.setBookingDate(request.getBookingDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.UNPAID);

        List<com.pickleball.app.entity.BookingService> bookingServices = new ArrayList<>();

        // Add Services
        if (request.getServices() != null && !request.getServices().isEmpty()) {
            for (BookingServiceRequest sr : request.getServices()) {
                com.pickleball.app.entity.Service service = serviceRepository.findById(sr.getServiceId())
                        .orElseThrow(() -> new RuntimeException("Service not found: " + sr.getServiceId()));

                com.pickleball.app.entity.BookingService bs = new com.pickleball.app.entity.BookingService();
                bs.setBooking(booking);
                bs.setService(service);
                bs.setQuantity(sr.getQuantity());
                bs.setUnitPrice(service.getPrice());

                bookingServices.add(bs);

                // Add to total price
                totalPrice = totalPrice.add(service.getPrice().multiply(BigDecimal.valueOf(sr.getQuantity())));
            }
        }

        booking.setBookingServices(bookingServices);
        booking.setTotalPrice(totalPrice);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created successfully: bookingId={}, userId={}, courtId={}, totalPrice={}",
                saved.getBookingId(), user.getUserId(), court.getCourtId(), totalPrice);

        // Đánh dấu các time slots là đã được đặt (sử dụng overlappingSlots đã tìm ở
        // trên)
        if (!overlappingSlots.isEmpty()) {
            List<Long> slotIdsToBook = overlappingSlots.stream()
                    .map(com.pickleball.app.entity.TimeSlot::getSlotId)
                    .collect(Collectors.toList());
            timeSlotService.markSlotsAsBooked(slotIdsToBook, saved.getBookingId());
            log.info("Marked {} time slots as booked for bookingId={}", slotIdsToBook.size(), saved.getBookingId());

            // Release locks sau khi đặt thành công
            if (timeSlotLockService != null) {
                timeSlotLockService.releaseLocksBySlots(slotIdsToBook);
                log.info("Released locks for {} slots after successful booking", slotIdsToBook.size());
            }
        }

        // Notify
        String msg = "Bạn đã đặt sân " + court.getCourtName() + " thành công vào ngày " + request.getBookingDate();
        notificationService.createNotification(user.getUserId(), "Đặt sân thành công", msg, "BOOKING");
        try {
            emailService.sendSimpleMessage(user.getEmail(), "Xác nhận đặt sân", msg);
            log.debug("Email sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to user {}: {}", user.getEmail(), e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(User user) {
        return bookingRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByUser(User user) {
        log.info("Getting bookings for user: userId={}", user.getUserId());
        return bookingRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByManager(User manager) {
        if (manager.getRole() == UserRole.ADMIN) {
            return bookingRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return bookingRepository.findByManagerId(manager.getUserId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, User user) {
        log.info("Cancelling booking: bookingId={}, userId={}", id, user.getUserId());

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", id);
                    return new RuntimeException("Booking not found");
                });

        if (!booking.getUser().getUserId().equals(user.getUserId()) && user.getRole() != UserRole.ADMIN) {
            log.warn("User {} not authorized to cancel booking {}", user.getUserId(), id);
            throw new RuntimeException("Not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Cannot cancel booking {} in status {}", id, booking.getStatus());
            throw new RuntimeException("Cannot cancel booking in current status");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} status updated to CANCELLED", id);

        // Giải phóng time slots khi hủy booking
        // Nếu thất bại, throw exception để rollback transaction
        List<com.pickleball.app.entity.TimeSlot> bookedSlots = timeSlotRepository
                .findByBooking_BookingId(booking.getBookingId());
        if (!bookedSlots.isEmpty()) {
            List<Long> slotIds = bookedSlots.stream()
                    .map(com.pickleball.app.entity.TimeSlot::getSlotId)
                    .collect(Collectors.toList());
            try {
                timeSlotService.markSlotsAsAvailable(slotIds);
                log.info("Released {} time slots for booking {}", slotIds.size(), id);
            } catch (Exception e) {
                log.error("Failed to release time slots for booking {}: {}", id, e.getMessage(), e);
                // Throw exception để rollback transaction
                throw new RuntimeException("Failed to release time slots: " + e.getMessage(), e);
            }
        }

        // Notify
        notificationService.createNotification(booking.getUser().getUserId(), "Hủy đặt sân",
                "Đơn đặt sân #" + booking.getBookingId() + " đã bị hủy.", "BOOKING");
        try {
            emailService.sendSimpleMessage(booking.getUser().getEmail(), "Thông báo hủy sân",
                    "Bạn đã hủy thành công đơn đặt sân #" + booking.getBookingId());
            log.debug("Email sent to user: {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send email to user {}: {}", booking.getUser().getEmail(), e.getMessage());
            // Email failure không nên rollback transaction
        }
    }

    @Override
    public void checkIn(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PAID) {
            throw new RuntimeException("Booking must be confirmed or paid to check-in");
        }

        booking.setCheckedInAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, String status) {
        log.info("Updating booking status: bookingId={}, newStatus={}", id, status);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", id);
                    return new RuntimeException("Booking not found");
                });

        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            BookingStatus oldStatus = booking.getStatus();
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            log.info("Booking {} status updated from {} to {}", id, oldStatus, newStatus);

            // Nếu chuyển sang CANCELLED hoặc REJECTED, giải phóng time slots
            if ((newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.REJECTED)
                    && oldStatus != BookingStatus.CANCELLED && oldStatus != BookingStatus.REJECTED) {
                List<com.pickleball.app.entity.TimeSlot> bookedSlots = timeSlotRepository
                        .findByBooking_BookingId(booking.getBookingId());
                if (!bookedSlots.isEmpty()) {
                    List<Long> slotIds = bookedSlots.stream()
                            .map(com.pickleball.app.entity.TimeSlot::getSlotId)
                            .collect(Collectors.toList());
                    try {
                        timeSlotService.markSlotsAsAvailable(slotIds);
                        log.info("Released {} time slots for booking {}", slotIds.size(), id);
                    } catch (Exception e) {
                        log.error("Failed to release time slots for booking {}: {}", id, e.getMessage(), e);
                        // Throw exception để rollback transaction
                        throw new RuntimeException("Failed to release time slots: " + e.getMessage(), e);
                    }
                }
            }

            // Notify user
            String message = "";
            String title = "Cập nhật trạng thái đặt sân";

            if (newStatus == BookingStatus.CONFIRMED) {
                title = "Đặt sân đã được xác nhận";
                message = "Đơn đặt sân #" + booking.getBookingId() + " của bạn đã được quản lý xác nhận.";

                // Send email
                try {
                    emailService.sendSimpleMessage(booking.getUser().getEmail(), title, message);
                } catch (Exception e) {
                    log.error("Failed to send confirmation email", e);
                }
            } else if (newStatus == BookingStatus.REJECTED) {
                title = "Đặt sân bị từ chối";
                message = "Đơn đặt sân #" + booking.getBookingId()
                        + " của bạn đã bị từ chối. Tiền sẽ được hoàn lại (nếu đã thanh toán).";

                // Send email
                try {
                    emailService.sendSimpleMessage(booking.getUser().getEmail(), title, message);
                } catch (Exception e) {
                    log.error("Failed to send rejection email", e);
                }
            } else {
                message = "Đơn đặt sân #" + booking.getBookingId() + " đã được cập nhật trạng thái: " + status;
            }

            notificationService.createNotification(booking.getUser().getUserId(),
                    title,
                    message,
                    "BOOKING");
        } catch (IllegalArgumentException e) {
            log.error("Invalid booking status: {}", status);
            throw new RuntimeException("Invalid booking status: " + status);
        }

        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking entity) {
        List<BookingServiceResponse> services = new ArrayList<>();
        if (entity.getBookingServices() != null) {
            services = entity.getBookingServices().stream()
                    .map(bs -> BookingServiceResponse.builder()
                            .id(bs.getId())
                            .serviceId(bs.getService().getServiceId())
                            .serviceName(bs.getService().getServiceName())
                            .quantity(bs.getQuantity())
                            .unitPrice(bs.getUnitPrice())
                            .totalPrice(bs.getUnitPrice().multiply(BigDecimal.valueOf(bs.getQuantity())))
                            .build())
                    .collect(Collectors.toList());
        }

        return BookingResponse.builder()
                .bookingId(entity.getBookingId())
                .userId(entity.getUser().getUserId())
                .userName(entity.getUser().getFullName())
                .courtId(entity.getCourt().getCourtId())
                .courtName(entity.getCourt().getCourtName())
                .courtGroupName(entity.getCourt().getCourtGroup().getGroupName())
                .bookingDate(entity.getBookingDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .totalPrice(entity.getTotalPrice())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .checkedInAt(entity.getCheckedInAt())
                .services(services)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
