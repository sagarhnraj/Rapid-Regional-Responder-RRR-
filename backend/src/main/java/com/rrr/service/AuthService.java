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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
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

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().toLowerCase().trim();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new ConflictException("An account with this email already exists");
        }

        // STRICT SECURITY RULE: Hardcode role to CITIZEN for public registration.
        // Users cannot self-assign VOLUNTEER or ADMIN roles.
        String role = "CITIZEN";

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

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        // Validate Google ID Token via Google's tokeninfo API
        String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken();
        JsonNode googleUser;
        try {
            String jsonResponse = restTemplate.getForObject(tokenInfoUrl, String.class);
            googleUser = objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired Google authentication token");
        }

        String email = googleUser.path("email").asText("").toLowerCase().trim();
        String name = googleUser.path("name").asText(email);
        boolean emailVerified = googleUser.path("email_verified").asBoolean(false);

        if (email.isEmpty() || !emailVerified) {
            throw new BadRequestException("Google authentication failed: Email not verified by Google");
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            UserProfile profile = userProfileRepository.findById(existingUser.getId()).orElse(null);
            String displayName = profile != null ? profile.getName() : existingUser.getEmail();

            UserPrincipal principal = UserPrincipal.create(existingUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            String jwt = tokenProvider.generateToken(authentication);

            return new AuthResponse(jwt, existingUser.getId(), existingUser.getEmail(), displayName, existingUser.getRole());
        }

        // New Google user — Check if application-specific required info (phone) is provided
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            // Signal frontend to prompt user for missing phone number
            return new AuthResponse(true, email, name);
        }

        // Create new account with CITIZEN role
        String randomPassword = UUID.randomUUID().toString();
        User newUser = new User(email, passwordEncoder.encode(randomPassword), "CITIZEN");
        newUser = userRepository.save(newUser);

        UserProfile profile = new UserProfile(newUser, name, request.getPhone().trim(), "");
        userProfileRepository.save(profile);

        UserPrincipal principal = UserPrincipal.create(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(jwt, newUser.getId(), newUser.getEmail(), profile.getName(), newUser.getRole());
    }
}
