package com.pickleball.app.dto.booking;

import com.pickleball.app.enums.BookingStatus;
import com.pickleball.app.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private String userName;
    private Long courtId;
    private String courtName;
    private String courtGroupName;

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private BigDecimal totalPrice;
    private BookingStatus status;
    private PaymentStatus paymentStatus;

    private LocalDateTime checkedInAt;
    private List<BookingServiceResponse> services;

    private LocalDateTime createdAt;
}
