package com.rrr.service;

import com.rrr.dto.EmergencyContactDto;
import com.rrr.dto.UserProfileDto;
import com.rrr.exception.ResourceNotFoundException;
import com.rrr.model.EmergencyContact;
import com.rrr.model.UserProfile;
import com.rrr.repository.EmergencyContactRepository;
import com.rrr.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmergencyContactRepository emergencyContactRepository;

    public UserProfileDto getProfile(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return new UserProfileDto(profile.getUserId(), profile.getName(), profile.getPhone(), profile.getMedicalInfo());
    }

    @Transactional
    public UserProfileDto updateProfile(UUID userId, UserProfileDto dto) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUserId(userId);
                    return p;
                });

        if (dto.getName() != null) profile.setName(dto.getName());
        if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
        if (dto.getMedicalInfo() != null) profile.setMedicalInfo(dto.getMedicalInfo());

        profile = userProfileRepository.save(profile);
        return new UserProfileDto(profile.getUserId(), profile.getName(), profile.getPhone(), profile.getMedicalInfo());
    }

    public List<EmergencyContactDto> getEmergencyContacts(UUID userId) {
        return emergencyContactRepository.findByUserIdOrderByIsPrimaryDescCreatedAtDesc(userId)
                .stream()
                .map(this::mapContactToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmergencyContactDto addEmergencyContact(UUID userId, EmergencyContactDto dto) {
        if (Boolean.TRUE.equals(dto.getIsPrimary())) {
            unsetPrimaryContacts(userId);
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setUserId(userId);
        contact.setName(dto.getName());
        contact.setPhone(dto.getPhone());
        contact.setRelationship(dto.getRelationship() != null ? dto.getRelationship() : "Friend");
        contact.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false);

        contact = emergencyContactRepository.save(contact);
        return mapContactToDto(contact);
    }

    @Transactional
    public void deleteEmergencyContact(UUID contactId, UUID userId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency contact not found"));

        if (!contact.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Emergency contact not found");
        }

        emergencyContactRepository.delete(contact);
    }

    private void unsetPrimaryContacts(UUID userId) {
        List<EmergencyContact> contacts = emergencyContactRepository.findByUserIdOrderByIsPrimaryDescCreatedAtDesc(userId);
        for (EmergencyContact c : contacts) {
            c.setIsPrimary(false);
        }
        emergencyContactRepository.saveAll(contacts);
    }

    private EmergencyContactDto mapContactToDto(EmergencyContact c) {
        EmergencyContactDto dto = new EmergencyContactDto();
        dto.setId(c.getId());
        dto.setUserId(c.getUserId());
        dto.setName(c.getName());
        dto.setPhone(c.getPhone());
        dto.setRelationship(c.getRelationship());
        dto.setIsPrimary(c.getIsPrimary());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
