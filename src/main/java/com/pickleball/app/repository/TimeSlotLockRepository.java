package com.pickleball.app.repository;

import com.pickleball.app.entity.TimeSlotLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotLockRepository extends JpaRepository<TimeSlotLock, Long> {

    /**
     * Tìm lock của một slot còn hiệu lực (chưa hết hạn)
     */
    @Query("SELECT l FROM TimeSlotLock l WHERE l.timeSlot.slotId = :slotId " +
           "AND l.expiresAt > :now")
    Optional<TimeSlotLock> findActiveLockBySlotId(@Param("slotId") Long slotId, @Param("now") LocalDateTime now);

    /**
     * Tìm tất cả locks của một user còn hiệu lực
     */
    @Query("SELECT l FROM TimeSlotLock l WHERE l.user.userId = :userId " +
           "AND l.expiresAt > :now")
    List<TimeSlotLock> findActiveLocksByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Tìm tất cả locks của một danh sách slots còn hiệu lực
     */
    @Query("SELECT l FROM TimeSlotLock l WHERE l.timeSlot.slotId IN :slotIds " +
           "AND l.expiresAt > :now")
    List<TimeSlotLock> findActiveLocksBySlotIds(@Param("slotIds") List<Long> slotIds, @Param("now") LocalDateTime now);

    /**
     * Xóa tất cả locks đã hết hạn
     * @return Số lượng locks đã xóa
     */
    @Modifying
    @Query("DELETE FROM TimeSlotLock l WHERE l.expiresAt <= :now")
    int deleteExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Xóa lock của một slot
     */
    @Modifying
    @Query("DELETE FROM TimeSlotLock l WHERE l.timeSlot.slotId = :slotId")
    void deleteBySlotId(@Param("slotId") Long slotId);

    /**
     * Xóa tất cả locks của một user
     */
    @Modifying
    @Query("DELETE FROM TimeSlotLock l WHERE l.user.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Xóa tất cả locks của một danh sách slots
     */
    @Modifying
    @Query("DELETE FROM TimeSlotLock l WHERE l.timeSlot.slotId IN :slotIds")
    void deleteBySlotIds(@Param("slotIds") List<Long> slotIds);
}

