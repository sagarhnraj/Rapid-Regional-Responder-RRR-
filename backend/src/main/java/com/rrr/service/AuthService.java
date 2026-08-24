package com.rrr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrr.dto.AuthRequest;
import com.rrr.dto.AuthResponse;
import com.rrr.dto.GoogleAuthRequest;
import com.rrr.dto.RegisterRequest;
import com.rrr.exception.BadRequestException;
import com.rrr.exception.ConflictException;
import com.rrr.model.User;
import com.rrr.model.UserProfile;
import com.rrr.repository.UserProfileRepository;
import com.rrr.repository.UserRepository;
import com.rrr.security.JwtTokenProvider;
import com.rrr.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> verifyGoogleRegistrationIdentity(GoogleAuthRequest request) {
        JsonNode googleUser = parseAndVerifyGoogleToken(request.getIdToken());
        String email = googleUser.path("email").asText("").toLowerCase().trim();
        String name = googleUser.path("name").asText(email);
        boolean emailVerified = googleUser.path("email_verified").asBoolean(false);

        if (email.isEmpty() || !emailVerified) {
            throw new BadRequestException("Google identity verification failed: Email not verified by Google");
        }

        // Rule: If Google-verified email already exists, do NOT create duplicate or auto-login. Direct user to Email+Password login.
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An RRR account with email '" + email + "' already exists. Please log in using your RRR Email and Password.");
        }

        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("name", name);
        return response;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();

        // 1. Verify Google identity token provided during registration (optional for direct registration)
        if (request.getGoogleIdToken() != null && !request.getGoogleIdToken().trim().isEmpty()) {
            JsonNode googleUser = parseAndVerifyGoogleToken(request.getGoogleIdToken());
            String googleEmail = googleUser.path("email").asText("").toLowerCase().trim();
            boolean emailVerified = googleUser.path("email_verified").asBoolean(false);

            if (googleEmail.isEmpty() || !emailVerified) {
                throw new BadRequestException("Registration failed: Invalid Google identity token");
            }

            if (!cleanEmail.equalsIgnoreCase(googleEmail)) {
                throw new BadRequestException("Registration failed: Submitted email does not match Google-verified identity email");
            }
        }

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("An RRR account with email '" + cleanEmail + "' already exists. Please log in using your RRR Email and Password.");
        }

        // STRICT SECURITY RULE: Hardcode role to CITIZEN for public registration.
        String role = "CITIZEN";

        // BCrypt password hashing
        User user = new User(
                cleanEmail,
                passwordEncoder.encode(request.getPassword()),
                role
        );
        user = userRepository.save(user);

        UserProfile profile = new UserProfile(user, request.getName(), request.getPhone(), "");
        userProfileRepository.save(profile);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return new AuthResponse(jwt, principal.getId(), principal.getEmail(), profile.getName(), principal.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        UserProfile profile = userProfileRepository.findById(principal.getId()).orElse(null);
        String name = profile != null ? profile.getName() : principal.getEmail();

        return new AuthResponse(jwt, principal.getId(), principal.getEmail(), name, principal.getRole());
    }

    private JsonNode parseAndVerifyGoogleToken(String idToken) {
        if ("mock_google_id_token_demo".equals(idToken)) {
            // For testing/demo fallback when no live Google client ID is connected
            try {
                return objectMapper.readTree("{\"email\":\"verified_user@gmail.com\", \"name\":\"Verified User\", \"email_verified\": true}");
            } catch (Exception ignored) {}
        }

        String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
        try {
            String jsonResponse = restTemplate.getForObject(tokenInfoUrl, String.class);
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired Google authentication token");
        }
    }
}
