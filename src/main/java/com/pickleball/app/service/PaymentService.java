package com.pickleball.app.service;

import com.pickleball.app.entity.Payment;

public interface PaymentService {
    String createPaymentUrl(Long bookingId, String method);

    Payment processPaymentCallback(String transactionRef, String statusCode);
}
