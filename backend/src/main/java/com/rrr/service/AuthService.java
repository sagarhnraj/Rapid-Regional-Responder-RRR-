package com.rrr.service;

import com.rrr.dto.AuthRequest;
import com.rrr.dto.AuthResponse;
import com.rrr.dto.RegisterRequest;
import com.rrr.exception.ConflictException;
import com.rrr.model.User;
import com.rrr.model.UserProfile;
import com.rrr.model.VolunteerProfile;
import com.rrr.repository.UserProfileRepository;
import com.rrr.repository.UserRepository;
import com.rrr.repository.VolunteerProfileRepository;
import com.rrr.security.JwtTokenProvider;
import com.rrr.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private VolunteerProfileRepository volunteerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("An account with this email already exists");
        }

        String role = "CITIZEN";
        if (request.getRole() != null && (request.getRole().equalsIgnoreCase("VOLUNTEER") || request.getRole().equalsIgnoreCase("ADMIN"))) {
            role = request.getRole().toUpperCase();
        }

        User user = new User(
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        user = userRepository.save(user);

        UserProfile profile = new UserProfile(user, request.getName(), request.getPhone(), "");
        userProfileRepository.save(profile);

        if ("VOLUNTEER".equals(role)) {
            VolunteerProfile volunteerProfile = new VolunteerProfile(user);
            volunteerProfileRepository.save(volunteerProfile);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return new AuthResponse(jwt, principal.getId(), principal.getEmail(), profile.getName(), principal.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        UserProfile profile = userProfileRepository.findById(principal.getId()).orElse(null);
        String name = profile != null ? profile.getName() : principal.getEmail();

        return new AuthResponse(jwt, principal.getId(), principal.getEmail(), name, principal.getRole());
    }
}
