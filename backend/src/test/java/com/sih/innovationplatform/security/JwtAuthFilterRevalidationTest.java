package com.sih.innovationplatform.security;

import com.sih.innovationplatform.user.User;
import com.sih.innovationplatform.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterRevalidationTest {

    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private UserRepository userRepository;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthFilter(jwtService, userDetailsService, userRepository);
    }

    @Test
    void authoritiesAndUserIdComeFromDatabaseNotToken() throws Exception {
        Claims claims = Jwts.claims()
                .subject("owner@example.com")
                .add("uid", 1L)
                .add("role", "PLATFORM_ADMIN")
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("owner@example.com");
        user.setStatus("ACTIVE");
        UserDetails details = new org.springframework.security.core.userdetails.User(
                "owner@example.com", "hash", List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));

        when(jwtService.isValid("token")).thenReturn(true);
        when(jwtService.parse("token")).thenReturn(claims);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("owner@example.com")).thenReturn(details);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus(), response.getContentAsString());
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(1L, auth.getDetails());
        assertFalse(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
        verify(chain).doFilter(request, response);
    }

    @Test
    void staleTokenRoleIsRejectedWhenUserMismatches() throws Exception {
        Claims claims = Jwts.claims()
                .subject("owner@example.com")
                .add("uid", 99L)
                .build();

        User user = new User();
        user.setId(1L);
        user.setEmail("owner@example.com");
        user.setStatus("ACTIVE");

        when(jwtService.isValid("token")).thenReturn(true);
        when(jwtService.parse("token")).thenReturn(claims);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(any(), any());
    }
}
