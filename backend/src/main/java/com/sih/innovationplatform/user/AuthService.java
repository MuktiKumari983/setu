package com.sih.innovationplatform.user;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> REGISTRABLE_ROLES =
            Set.of("CITIZEN", "STUDENT", "FACULTY", "UNIVERSITY_ADMIN", "INDUSTRY");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        String roleName = request.role() == null || request.role().isBlank()
                ? "CITIZEN" : request.role().trim().toUpperCase();
        if (!REGISTRABLE_ROLES.contains(roleName)) {
            throw ApiException.badRequest("Role not allowed for self-registration: " + roleName);
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
            throw ApiException.badRequest("Invalid email or password");
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

    public UserDto me(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRoleName(), user.getLinkedUniversityId());
    }

    public record RegisterRequest(String name, String email, String password, String phone, String role, Long universityId) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record UserDto(Long id, String name, String email, String role, Long linkedUniversityId) {
    }

    public record AuthResponse(String token, UserDto user) {
    }
}