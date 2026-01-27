package com.pickleball.app.service;

import com.pickleball.app.entity.TimeSlotLock;
import com.pickleball.app.entity.User;

import java.time.Duration;
import java.util.List;

/**
 * Service quản lý temporary lock cho time slots
 */
public interface TimeSlotLockService {
    
    /**
     * Lock một time slot cho user trong một khoảng thời gian
     * @param slotId ID của time slot
     * @param user User đang lock
     * @param duration Thời gian lock (mặc định 5 phút)
     * @return TimeSlotLock đã tạo
     * @throws RuntimeException nếu slot đã bị lock bởi user khác
     */
    TimeSlotLock lockSlot(Long slotId, User user, Duration duration);
    
    /**
     * Lock nhiều time slots cho user
     * @param slotIds Danh sách ID của time slots
     * @param user User đang lock
     * @param duration Thời gian lock (mặc định 5 phút)
     * @return Danh sách TimeSlotLock đã tạo
     * @throws RuntimeException nếu bất kỳ slot nào đã bị lock bởi user khác
     */
    List<TimeSlotLock> lockSlots(List<Long> slotIds, User user, Duration duration);
    
    /**
     * Kiểm tra xem slot có đang bị lock bởi user khác không
     * @param slotId ID của time slot
     * @param userId ID của user (null nếu không quan tâm user nào lock)
     * @return true nếu slot đang bị lock bởi user khác
     */
    boolean isSlotLockedByOtherUser(Long slotId, Long userId);
    
    /**
     * Kiểm tra xem slot có đang bị lock không (bởi bất kỳ user nào)
     * @param slotId ID của time slot
     * @return true nếu slot đang bị lock
     */
    boolean isSlotLocked(Long slotId);
    
    /**
     * Release lock của một slot
     * @param slotId ID của time slot
     */
    void releaseLock(Long slotId);
    
    /**
     * Release tất cả locks của một user
     * @param userId ID của user
     */
    void releaseLocksByUser(Long userId);
    
    /**
     * Release tất cả locks của một danh sách slots
     * @param slotIds Danh sách ID của time slots
     */
    void releaseLocksBySlots(List<Long> slotIds);
    
    /**
     * Gia hạn lock của một slot
     * @param slotId ID của time slot
     * @param user User đang giữ lock
     * @param additionalDuration Thời gian gia hạn thêm
     * @return TimeSlotLock đã được gia hạn
     * @throws RuntimeException nếu user không phải là người đang giữ lock
     */
    TimeSlotLock extendLock(Long slotId, User user, Duration additionalDuration);
    
    /**
     * Cleanup tất cả locks đã hết hạn
     * Nên được gọi định kỳ bởi scheduled task
     */
    void cleanupExpiredLocks();
    
    /**
     * Lấy thông tin lock của một slot
     * @param slotId ID của time slot
     * @return UserId của người đang giữ lock, null nếu không bị lock
     */
    Long getLockedByUserId(Long slotId);
    
    /**
     * Lấy thông tin lock của nhiều slots
     * @param slotIds Danh sách ID của time slots
     * @return Map<slotId, userId> - userId là null nếu slot không bị lock
     */
    java.util.Map<Long, Long> getLockedByUserIds(List<Long> slotIds);
}

