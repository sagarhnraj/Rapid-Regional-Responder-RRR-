package com.rrr.repository;

import com.rrr.model.VolunteerResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VolunteerResponseRepository extends JpaRepository<VolunteerResponse, UUID> {
    List<VolunteerResponse> findBySosRequestId(UUID sosRequestId);
    List<VolunteerResponse> findByVolunteerIdOrderByCreatedAtDesc(UUID volunteerId);
    Optional<VolunteerResponse> findBySosRequestIdAndVolunteerId(UUID sosRequestId, UUID volunteerId);
    boolean existsBySosRequestIdAndVolunteerId(UUID sosRequestId, UUID volunteerId);
}
