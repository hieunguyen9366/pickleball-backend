package com.pickleball.app.repository;

import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtPriceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtPriceConfigRepository extends JpaRepository<CourtPriceConfig, Long> {
    List<CourtPriceConfig> findByCourt(Court court);

    List<CourtPriceConfig> findByCourt_CourtId(Long courtId);
}
