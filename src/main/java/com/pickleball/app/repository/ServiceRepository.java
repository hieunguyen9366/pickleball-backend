package com.pickleball.app.repository;

import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.Service;
import com.pickleball.app.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByCourtGroup(CourtGroup courtGroup);

    List<Service> findByCourtGroupAndStatus(CourtGroup courtGroup, ServiceStatus status);
}
