package com.cropdeal.gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "CropDealSuperSecretKeyForJWT2026ChangeMe";
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtService(SECRET));

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mapsAdminRoleFromJwt() throws Exception {
        authenticate(token("admin@example.com", "ADMIN"));
        assertEquals("admin@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(hasAuthority("ROLE_ADMIN"));
    }

    @Test
    void mapsMultipleCommaSeparatedRoles() throws Exception {
        authenticate(token("user@example.com", "FARMER,DEALER"));
        assertTrue(hasAuthority("ROLE_FARMER"));
        assertTrue(hasAuthority("ROLE_DEALER"));
    }

    @Test
    void doesNotDoublePrefixRoleClaims() throws Exception {
        authenticate(token("admin@example.com", "ROLE_ADMIN"));
        assertTrue(hasAuthority("ROLE_ADMIN"));
        assertFalse(hasAuthority("ROLE_ROLE_ADMIN"));
    }

    @Test
    void invalidTokenDoesNotAuthenticateRequest() throws Exception {
        authenticate("invalid-token");
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == null);
    }

    private void authenticate(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilterInternal(request, new MockHttpServletResponse(), (servletRequest, servletResponse) -> { });
    }

    private boolean hasAuthority(String authority) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    private String token(String subject, String roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder().subject(subject).claim("roles", roles).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000)).signWith(key).compact();
    }
}