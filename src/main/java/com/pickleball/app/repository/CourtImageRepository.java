package com.pickleball.app.repository;

import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {

    List<CourtImage> findByCourtOrderBySortOrderAscImageIdAsc(Court court);
}

