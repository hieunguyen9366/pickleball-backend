package com.pickleball.app.repository;

import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtGroupRepository extends JpaRepository<CourtGroup, Long> {
    List<CourtGroup> findByManager(User manager);

    List<CourtGroup> findByCityContainingIgnoreCase(String city);
}
