package com.sih.innovationplatform.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthService.AuthResponse register(@Valid @RequestBody AuthService.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthService.AuthResponse login(@Valid @RequestBody AuthService.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserDetails principal) {
        Long userId = (Long) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        AuthService.UserDto user = authService.me(userId);
        return Map.of("user", user);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','GOVERNMENT_ADMIN')")
    public AuthService.UserDto userById(@PathVariable Long id) {
        return authService.me(id);
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasAnyRole('PLATFORM_ADMIN','GOVERNMENT_ADMIN')")
    public AuthService.UserDto assignRole(@PathVariable Long id,
                                          @Valid @RequestBody AuthService.RoleAssignRequest request) {
        return authService.assignRole(id, request.role());
    }
}