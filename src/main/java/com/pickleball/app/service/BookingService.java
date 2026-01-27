package com.pickleball.app.service;

import com.pickleball.app.dto.booking.BookingRequest;
import com.pickleball.app.dto.booking.BookingResponse;
import com.pickleball.app.entity.User;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(User user, BookingRequest request);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getMyBookings(User user);

    List<BookingResponse> getBookingsByUser(User user);

    List<BookingResponse> getBookingsByManager(User manager);

    void cancelBooking(Long id, User user);

    void checkIn(Long id);
    
    BookingResponse updateBookingStatus(Long id, String status);
}
