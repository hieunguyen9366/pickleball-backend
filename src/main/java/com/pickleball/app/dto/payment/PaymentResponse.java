package com.pickleball.app.dto.payment;

import com.pickleball.app.enums.PaymentMethod;
import com.pickleball.app.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private String transactionRef;
    private PaymentStatus status;
    private LocalDateTime paymentTime;
}

