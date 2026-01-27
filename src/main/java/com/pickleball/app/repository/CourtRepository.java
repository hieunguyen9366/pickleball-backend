package com.pickleball.app.repository;

import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.enums.CourtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtRepository extends JpaRepository<Court, Long> {
    List<Court> findByCourtGroup(CourtGroup courtGroup);

    List<Court> findByStatus(CourtStatus status);
}
