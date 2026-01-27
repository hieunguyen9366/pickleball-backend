package com.pickleball.app.service.impl;

import com.pickleball.app.entity.TimeSlot;
import com.pickleball.app.entity.TimeSlotLock;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.TimeSlotLockRepository;
import com.pickleball.app.repository.TimeSlotRepository;
import com.pickleball.app.service.TimeSlotLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotLockServiceImpl implements TimeSlotLockService {

    private final TimeSlotLockRepository lockRepository;
    private final TimeSlotRepository timeSlotRepository;
    
    // Mặc định lock 5 phút
    private static final Duration DEFAULT_LOCK_DURATION = Duration.ofMinutes(5);

    @Override
    @Transactional
    public TimeSlotLock lockSlot(Long slotId, User user, Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        
        // Kiểm tra slot có tồn tại không
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> {
                    log.error("Time slot not found: {}", slotId);
                    return new RuntimeException("Time slot not found");
                });
        
        // Kiểm tra slot đã bị lock chưa
        Optional<TimeSlotLock> existingLock = lockRepository.findActiveLockBySlotId(slotId, now);
        if (existingLock.isPresent()) {
            TimeSlotLock lock = existingLock.get();
            // Nếu cùng user, cho phép extend lock
            if (lock.getUser().getUserId().equals(user.getUserId())) {
                log.debug("User {} already has lock on slot {}, extending...", user.getUserId(), slotId);
                return extendLock(slotId, user, duration != null ? duration : DEFAULT_LOCK_DURATION);
            } else {
                log.warn("Slot {} is already locked by user {} until {}", 
                        slotId, lock.getUser().getUserId(), lock.getExpiresAt());
                throw new RuntimeException("Khung giờ này đang được người khác giữ. Vui lòng chọn khung giờ khác.");
            }
        }
        
        // Tạo lock mới
        Duration lockDuration = duration != null ? duration : DEFAULT_LOCK_DURATION;
        LocalDateTime expiresAt = now.plus(lockDuration);
        
        TimeSlotLock lock = TimeSlotLock.builder()
                .timeSlot(slot)
                .user(user)
                .expiresAt(expiresAt)
                .build();
        
        TimeSlotLock saved = lockRepository.save(lock);
        log.info("Locked slot {} for user {} until {}", slotId, user.getUserId(), expiresAt);
        
        return saved;
    }

    @Override
    @Transactional
    public List<TimeSlotLock> lockSlots(List<Long> slotIds, User user, Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        List<TimeSlotLock> locks = new ArrayList<>();
        
        // Kiểm tra tất cả slots có bị lock bởi user khác không
        List<TimeSlotLock> existingLocks = lockRepository.findActiveLocksBySlotIds(slotIds, now);
        for (TimeSlotLock existingLock : existingLocks) {
            if (!existingLock.getUser().getUserId().equals(user.getUserId())) {
                log.warn("Slot {} is already locked by user {}", 
                        existingLock.getTimeSlot().getSlotId(), existingLock.getUser().getUserId());
                throw new RuntimeException("Một số khung giờ đang được người khác giữ. Vui lòng chọn lại.");
            }
        }
        
        // Lock tất cả slots
        Duration lockDuration = duration != null ? duration : DEFAULT_LOCK_DURATION;
        LocalDateTime expiresAt = now.plus(lockDuration);
        
        for (Long slotId : slotIds) {
            TimeSlot slot = timeSlotRepository.findById(slotId)
                    .orElseThrow(() -> new RuntimeException("Time slot not found: " + slotId));
            
            // Nếu đã có lock của cùng user, skip
            boolean alreadyLocked = existingLocks.stream()
                    .anyMatch(l -> l.getTimeSlot().getSlotId().equals(slotId) 
                            && l.getUser().getUserId().equals(user.getUserId()));
            
            if (!alreadyLocked) {
                TimeSlotLock lock = TimeSlotLock.builder()
                        .timeSlot(slot)
                        .user(user)
                        .expiresAt(expiresAt)
                        .build();
                locks.add(lockRepository.save(lock));
            }
        }
        
        log.info("Locked {} slots for user {} until {}", locks.size(), user.getUserId(), expiresAt);
        return locks;
    }

    @Override
    public boolean isSlotLockedByOtherUser(Long slotId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<TimeSlotLock> lock = lockRepository.findActiveLockBySlotId(slotId, now);
        return lock.isPresent() && !lock.get().getUser().getUserId().equals(userId);
    }

    @Override
    public boolean isSlotLocked(Long slotId) {
        LocalDateTime now = LocalDateTime.now();
        return lockRepository.findActiveLockBySlotId(slotId, now).isPresent();
    }

    @Override
    @Transactional
    public void releaseLock(Long slotId) {
        lockRepository.deleteBySlotId(slotId);
        log.debug("Released lock for slot {}", slotId);
    }

    @Override
    @Transactional
    public void releaseLocksByUser(Long userId) {
        lockRepository.deleteByUserId(userId);
        log.info("Released all locks for user {}", userId);
    }

    @Override
    @Transactional
    public void releaseLocksBySlots(List<Long> slotIds) {
        lockRepository.deleteBySlotIds(slotIds);
        log.info("Released locks for {} slots", slotIds.size());
    }

    @Override
    @Transactional
    public TimeSlotLock extendLock(Long slotId, User user, Duration additionalDuration) {
        LocalDateTime now = LocalDateTime.now();
        TimeSlotLock lock = lockRepository.findActiveLockBySlotId(slotId, now)
                .orElseThrow(() -> new RuntimeException("No active lock found for slot: " + slotId));
        
        if (!lock.getUser().getUserId().equals(user.getUserId())) {
            log.warn("User {} tried to extend lock held by user {}", 
                    user.getUserId(), lock.getUser().getUserId());
            throw new RuntimeException("Bạn không có quyền gia hạn lock này");
        }
        
        // Gia hạn lock
        LocalDateTime newExpiresAt = lock.getExpiresAt().plus(additionalDuration);
        lock.setExpiresAt(newExpiresAt);
        
        TimeSlotLock saved = lockRepository.save(lock);
        log.info("Extended lock for slot {} until {}", slotId, newExpiresAt);
        
        return saved;
    }

    @Override
    @Transactional
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = lockRepository.deleteExpiredLocks(now);
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired locks", deletedCount);
        }
    }

    @Override
    public Long getLockedByUserId(Long slotId) {
        LocalDateTime now = LocalDateTime.now();
        Optional<TimeSlotLock> lock = lockRepository.findActiveLockBySlotId(slotId, now);
        return lock.map(l -> l.getUser().getUserId()).orElse(null);
    }

    @Override
    public Map<Long, Long> getLockedByUserIds(List<Long> slotIds) {
        if (slotIds == null || slotIds.isEmpty()) {
            return new HashMap<>();
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<TimeSlotLock> locks = lockRepository.findActiveLocksBySlotIds(slotIds, now);
        
        Map<Long, Long> result = new HashMap<>();
        for (Long slotId : slotIds) {
            result.put(slotId, null); // Default: not locked
        }
        
        for (TimeSlotLock lock : locks) {
            result.put(lock.getTimeSlot().getSlotId(), lock.getUser().getUserId());
        }
        
        return result;
    }
}

