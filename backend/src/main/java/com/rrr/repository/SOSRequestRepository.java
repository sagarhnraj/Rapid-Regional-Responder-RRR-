package com.rrr.repository;

import com.rrr.model.SOSRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SOSRequestRepository extends JpaRepository<SOSRequest, UUID> {

    List<SOSRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<SOSRequest> findFirstByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, List<String> statuses);

    List<SOSRequest> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    long countByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SOSRequest s WHERE s.id = :id")
    Optional<SOSRequest> findByIdForUpdate(@Param("id") UUID id);
}
