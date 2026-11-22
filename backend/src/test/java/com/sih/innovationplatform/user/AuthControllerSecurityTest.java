package com.sih.innovationplatform.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.sih.innovationplatform.security.JwtAuthFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerSecurityTest.TestSecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                            .requestMatchers("/api/auth/users/**")
                            .hasAnyRole("PLATFORM_ADMIN", "GOVERNMENT_ADMIN")
                            .anyRequest().authenticated())
                    .build();
        }
    }

    @Test
    void anonymousRegistrationIsAllowed() throws Exception {
        when(authService.register(any(AuthService.RegisterRequest.class)))
                .thenReturn(new AuthService.AuthResponse("token",
                        new AuthService.UserDto(1L, "Citizen", "citizen@example.com", "CITIZEN", null)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Citizen\",\"email\":\"citizen@example.com\","
                                + "\"password\":\"password123\",\"role\":\"STUDENT\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void citizenCannotAssignRoles() throws Exception {
        mockMvc.perform(put("/api/auth/users/2/role")
                        .with(user("citizen").roles("CITIZEN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"UNIVERSITY_ADMIN\"}"))
                .andExpect(status().isForbidden());

        verify(authService, never()).assignRole(any(), any());
    }

    @Test
    void platformAdminCanAssignRoles() throws Exception {
        when(authService.assignRole(eq(2L), eq("UNIVERSITY_ADMIN")))
                .thenReturn(new AuthService.UserDto(2L, "Target", "target@example.com", "UNIVERSITY_ADMIN", null));

        mockMvc.perform(put("/api/auth/users/2/role")
                        .with(user("admin").roles("PLATFORM_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"UNIVERSITY_ADMIN\"}"))
                .andExpect(status().isOk());
    }
}
