package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.payment.PaymentResponse;
import com.pickleball.app.entity.Payment;
import com.pickleball.app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@RequestParam Long bookingId,
            @RequestParam String method) {
        return ResponseEntity
                .ok(ApiResponse.success("Payment URL created", paymentService.createPaymentUrl(bookingId, method)));
    }

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(@RequestParam String ref, @RequestParam String status) {
        Payment payment = paymentService.processPaymentCallback(ref, status);
        PaymentResponse response = toPaymentResponse(payment);
        return ResponseEntity
                .ok(ApiResponse.success("Payment processed", response));
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionRef(payment.getTransactionRef())
                .status(payment.getStatus())
                .paymentTime(payment.getPaymentTime())
                .build();
    }
}
