package com.rrr.repository;

import com.rrr.model.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, UUID> {
    List<IncidentHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<IncidentHistory> findByVolunteerIdOrderByCreatedAtDesc(UUID volunteerId);
    List<IncidentHistory> findAllByOrderByCreatedAtDesc();
}
