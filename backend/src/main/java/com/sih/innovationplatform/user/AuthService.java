package com.sih.innovationplatform.user;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.common.Roles;
import com.sih.innovationplatform.security.JwtService;
import com.sih.innovationplatform.university.UniversityRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> REGISTRABLE_ROLES = Set.of(
            Roles.CITIZEN, Roles.STUDENT, Roles.FACULTY, Roles.INDUSTRY);

    private static final Set<String> ASSIGNABLE_ROLES = Set.of(
            Roles.CITIZEN, Roles.STUDENT, Roles.FACULTY, Roles.INDUSTRY,
            Roles.UNIVERSITY_ADMIN, Roles.PLATFORM_ADMIN, Roles.GOVERNMENT_ADMIN);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UniversityRepository universityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        String roleName = normalizeRole(request.role(), Roles.CITIZEN);
        if (!REGISTRABLE_ROLES.contains(roleName)) {
            throw ApiException.badRequest("Role not allowed for self-registration: " + roleName);
        }
        if (request.universityId() != null
                && universityRepository.findById(request.universityId()).isEmpty()) {
            throw ApiException.badRequest("Unknown university");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw ApiException.badRequest("Name is required");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw ApiException.badRequest("Password must be at least 6 characters");
        }
        String email = request.email() == null ? "" : request.email().trim().toLowerCase();
        if (email.isBlank() || userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName, roleName + " user")));

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setLinkedUniversityId(request.universityId());
        user.setRole(role);
        User saved = userRepository.save(user);
        return buildResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.email() == null || request.password() == null) {
            throw ApiException.badRequest("Email and password are required");
        }
        String email = request.email().trim().toLowerCase();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user.getEmail(), user.getRoleName(), user.getId());
        return new AuthResponse(token, new UserDto(
                user.getId(), user.getName(), user.getEmail(), user.getRoleName(), user.getLinkedUniversityId()));
    }

    public UserDto assignRole(Long id, String role) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        String roleName = normalizeRole(role, null);
        if (roleName == null || !ASSIGNABLE_ROLES.contains(roleName)) {
            throw ApiException.badRequest("Unknown role: " + roleName);
        }
        Role target = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName, roleName + " user")));
        user.setRole(target);
        userRepository.save(user);
        return me(id);
    }

    public UserDto me(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRoleName(), user.getLinkedUniversityId());
    }

    private String normalizeRole(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return raw.trim().toUpperCase();
    }

    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            @Size(max = 150, message = "Name must be 150 characters or fewer")
            String name,
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            @Size(max = 150, message = "Email must be 150 characters or fewer")
            String email,
            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
            String password,
            @Size(max = 30, message = "Phone must be 30 characters or fewer")
            String phone,
            String role,
            Long universityId
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,
            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    public record RoleAssignRequest(
            @NotBlank(message = "Role is required")
            @Size(max = 50, message = "Role must be 50 characters or fewer")
            String role
    ) {
    }

    public record UserDto(Long id, String name, String email, String role, Long linkedUniversityId) {
    }

    public record AuthResponse(String token, UserDto user) {
    }
}