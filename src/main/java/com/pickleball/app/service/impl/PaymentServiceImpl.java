package com.pickleball.app.service.impl;

import com.pickleball.app.entity.Booking;
import com.pickleball.app.entity.Payment;
import com.pickleball.app.enums.BookingStatus;
import com.pickleball.app.enums.PaymentMethod;
import com.pickleball.app.enums.PaymentStatus;
import com.pickleball.app.repository.BookingRepository;
import com.pickleball.app.repository.PaymentRepository;
import com.pickleball.app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public String createPaymentUrl(Long bookingId, String method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Create pending payment record
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(PaymentMethod.valueOf(method.toUpperCase()));
        payment.setTransactionRef(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentTime(LocalDateTime.now());

        paymentRepository.save(payment);

        // In real implementation, call Momo/VNPay API here
        // For now, return a mock success URL
        return "https://api.pickleball.com/api/v1/payments/callback?ref=" + payment.getTransactionRef()
                + "&status=SUCCESS";
    }

    @Override
    @Transactional
    public Payment processPaymentCallback(String transactionRef, String statusCode) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("SUCCESS".equalsIgnoreCase(statusCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            // Update booking status
            Booking booking = payment.getBooking();
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setStatus(BookingStatus.PAID); // Wait for admin confirmation
            bookingRepository.save(booking);

        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }
}
