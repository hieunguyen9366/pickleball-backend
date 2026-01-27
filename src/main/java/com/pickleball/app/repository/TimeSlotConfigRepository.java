package com.pickleball.app.repository;

import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.TimeSlotConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotConfigRepository extends JpaRepository<TimeSlotConfig, Long> {

    /**
     * Tìm config cho sân cụ thể
     */
    Optional<TimeSlotConfig> findByCourtAndIsActiveTrue(Court court);

    /**
     * Tìm config cho cụm sân
     */
    Optional<TimeSlotConfig> findByCourtGroupAndIsActiveTrue(CourtGroup courtGroup);

    /**
     * Tìm tất cả configs theo sân
     */
    List<TimeSlotConfig> findByCourt(Court court);

    /**
     * Tìm tất cả configs theo cụm sân
     */
    List<TimeSlotConfig> findByCourtGroup(CourtGroup courtGroup);

    /**
     * Tìm config cho sân (ưu tiên config của sân, nếu không có thì lấy config của cụm sân)
     */
    @Query("SELECT c FROM TimeSlotConfig c WHERE " +
           "((c.court = :court AND c.isActive = true) OR " +
           "(c.courtGroup = :courtGroup AND c.court IS NULL AND c.isActive = true)) " +
           "ORDER BY CASE WHEN c.court IS NOT NULL THEN 1 ELSE 2 END")
    List<TimeSlotConfig> findConfigForCourt(@Param("court") Court court, @Param("courtGroup") CourtGroup courtGroup);

    /**
     * Kiểm tra xem đã có config active cho sân chưa
     */
    boolean existsByCourtAndIsActiveTrue(Court court);

    /**
     * Kiểm tra xem đã có config active cho cụm sân chưa
     */
    boolean existsByCourtGroupAndIsActiveTrue(CourtGroup courtGroup);
}

