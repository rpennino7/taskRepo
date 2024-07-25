package org.example.taskservice.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class JwtRequestFilterTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> httpEntityCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jwtRequestFilter = new JwtRequestFilter(restTemplate);
        jwtRequestFilter.authServiceUrl = "http://localhost:8080/auth";
    }

    @Test
    void testDoFilterInternalWithInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        JwtRequestFilter.ValidateTokenResponse validateTokenResponse = new JwtRequestFilter.ValidateTokenResponse();
        validateTokenResponse.setValid(false);

        when(restTemplate.exchange(eq("http://localhost:8080/auth/validate"), eq(HttpMethod.POST), any(HttpEntity.class), eq(JwtRequestFilter.ValidateTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(validateTokenResponse));
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void testDoFilterInternalWithValidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");

        JwtRequestFilter.ValidateTokenResponse validateTokenResponse = new JwtRequestFilter.ValidateTokenResponse();
        validateTokenResponse.setValid(true);

        when(restTemplate.exchange(eq("http://localhost:8080/auth/validate"), eq(HttpMethod.POST), any(HttpEntity.class), eq(JwtRequestFilter.ValidateTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(validateTokenResponse));

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken);
    }

    @Test
    void testDoFilterInternalWithoutAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternalWithAuthorizationHeaderNotStartingWithBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testValidateTokenWithValidToken() {
        JwtRequestFilter.ValidateTokenResponse validateTokenResponse = new JwtRequestFilter.ValidateTokenResponse();
        validateTokenResponse.setValid(true);

        when(restTemplate.exchange(eq("http://localhost:8080/auth/validate"), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(JwtRequestFilter.ValidateTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(validateTokenResponse));

        boolean isValid = jwtRequestFilter.validateToken("valid_token");

        assertTrue(isValid);

        HttpEntity<String> capturedEntity = httpEntityCaptor.getValue();
        String expectedRequestBody = "{\"token\": \"valid_token\"}";
        assertTrue(capturedEntity.getBody().contains(expectedRequestBody));
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        JwtRequestFilter.ValidateTokenResponse validateTokenResponse = new JwtRequestFilter.ValidateTokenResponse();
        validateTokenResponse.setValid(false);

        when(restTemplate.exchange(eq("http://localhost:8080/auth/validate"), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(JwtRequestFilter.ValidateTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(validateTokenResponse));

        boolean isValid = jwtRequestFilter.validateToken("invalid_token");

        assertTrue(!isValid);

        HttpEntity<String> capturedEntity = httpEntityCaptor.getValue();
        String expectedRequestBody = "{\"token\": \"invalid_token\"}";
        assertTrue(capturedEntity.getBody().contains(expectedRequestBody));
    }
}