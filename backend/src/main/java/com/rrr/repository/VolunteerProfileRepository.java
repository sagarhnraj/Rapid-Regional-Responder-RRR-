package com.rrr.repository;

import com.rrr.model.VolunteerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VolunteerProfileRepository extends JpaRepository<VolunteerProfile, UUID> {

    long countByIsAvailableTrue();

    @Query(value = """
        SELECT 
            v.user_id AS volunteerId,
            p.name AS volunteerName,
            p.phone AS volunteerPhone,
            v.latitude AS latitude,
            v.longitude AS longitude,
            ROUND((6371000 * acos(
                LEAST(1.0, GREATEST(-1.0,
                    cos(radians(:lat)) * cos(radians(v.latitude)) * 
                    cos(radians(v.longitude) - radians(:lng)) + 
                    sin(radians(:lat)) * sin(radians(v.latitude))
                ))
            ))::numeric, 1) AS distanceMeters
        FROM volunteer_profiles v
        JOIN user_profiles p ON v.user_id = p.user_id
        WHERE v.is_available = TRUE
          AND (6371000 * acos(
                LEAST(1.0, GREATEST(-1.0,
                    cos(radians(:lat)) * cos(radians(v.latitude)) * 
                    cos(radians(v.longitude) - radians(:lng)) + 
                    sin(radians(:lat)) * sin(radians(v.latitude))
                ))
          )) <= LEAST(v.max_range_meters, :systemMaxRadius)
        ORDER BY distanceMeters ASC
        """, nativeQuery = true)
    List<Object[]> findEligibleVolunteersWithinSystemRadius(
        @Param("lat") double lat,
        @Param("lng") double lng,
        @Param("systemMaxRadius") int systemMaxRadius
    );
}
