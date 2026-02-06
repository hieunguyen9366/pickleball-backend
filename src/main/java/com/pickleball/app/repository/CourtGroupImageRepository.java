package com.pickleball.app.repository;

import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.CourtGroupImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtGroupImageRepository extends JpaRepository<CourtGroupImage, Long> {

    List<CourtGroupImage> findByCourtGroupOrderBySortOrderAscImageIdAsc(CourtGroup courtGroup);
}

