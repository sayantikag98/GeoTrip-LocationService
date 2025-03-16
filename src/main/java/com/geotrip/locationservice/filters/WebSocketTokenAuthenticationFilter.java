package com.geotrip.locationservice.filters;

import com.geotrip.locationservice.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class WebSocketTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final Logger logger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            logger.info("Found token in query: {}", token);
            if (!jwtService.isTokenExpired(token)) {
                String email = jwtService.extractEmail(token);
                String role = jwtService.extractRole(token);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                // Only set if not already set
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("Authentication set for user: " + email + " with role: " + role);
                }
            } else {
                System.out.println("Token is expired.");
            }
        } else {
            System.out.println("No token found in query.");
        }
        filterChain.doFilter(request, response);
    }
}
