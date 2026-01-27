package com.pickleball.app.repository;

import com.pickleball.app.entity.Booking;
import com.pickleball.app.entity.User;
import com.pickleball.app.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByUser(User user);

        List<Booking> findByBookingDate(LocalDate date);

        List<Booking> findByStatus(BookingStatus status);

        // Find expired pending bookings
        List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);

        @Query("SELECT b FROM Booking b WHERE b.court.courtGroup.manager.userId = :managerId")
        List<Booking> findByManagerId(@Param("managerId") Long managerId);

        // Find conflicting bookings
        @Query("SELECT b FROM Booking b WHERE b.court.courtId = :courtId " +
                        "AND b.bookingDate = :date " +
                        "AND b.status != 'CANCELLED' " +
                        "AND b.status != 'REJECTED' " +
                        "AND ((b.startTime < :endTime AND b.endTime > :startTime) OR (b.startTime = :startTime))")
        List<Booking> findConflictingBookings(@Param("courtId") Long courtId,
                        @Param("date") LocalDate date,
                        @Param("startTime") java.time.LocalTime startTime,
                        @Param("endTime") java.time.LocalTime endTime);
}
