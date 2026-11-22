package com.sih.innovationplatform.user;

import com.sih.innovationplatform.common.ApiException;
import com.sih.innovationplatform.security.JwtService;
import com.sih.innovationplatform.university.UniversityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(userRepository, roleRepository, universityRepository,
                passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void universityAdminCannotSelfRegister() {
        AuthService.RegisterRequest request = new AuthService.RegisterRequest(
                "Admin", "admin2@example.com", "password123", null, "UNIVERSITY_ADMIN", null);

        ApiException exception = assertThrows(ApiException.class, () -> service.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unknownUniversityIsRejected() {
        AuthService.RegisterRequest request = new AuthService.RegisterRequest(
                "Student", "student2@example.com", "password123", null, "STUDENT", 99L);
        when(universityRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> service.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void adminCanAssignPrivilegedRole() {
        User user = new User();
        user.setId(5L);
        user.setName("Target");
        user.setEmail("target@example.com");
        Role targetRole = new Role("UNIVERSITY_ADMIN", "University coordinator");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("UNIVERSITY_ADMIN")).thenReturn(Optional.of(targetRole));

        AuthService.UserDto result = service.assignRole(5L, "university_admin");

        assertEquals("UNIVERSITY_ADMIN", result.role());
        verify(userRepository).save(user);
    }
}
