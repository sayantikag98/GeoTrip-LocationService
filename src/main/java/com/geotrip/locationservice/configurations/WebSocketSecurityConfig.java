package com.geotrip.locationservice.configurations;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;


@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> webSocketAuthorizationManager() {
        return (authentication, message) -> {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            String destination = accessor.getDestination();
            System.out.println("Authorization check for destination: " + destination);
            System.out.println("Authenticated authorities: " + authentication.get().getAuthorities());

            if (destination != null) {
                if (destination.startsWith("/app/driver/") && authentication.get().getAuthorities().stream()
                        .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_DRIVER"))) {
                    System.err.println("Access Denied for destination: " + destination);
                    throw new AccessDeniedException("Access Denied: Driver role required");
                }
                if (destination.startsWith("/app/passenger/") && authentication.get().getAuthorities().stream()
                        .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_PASSENGER"))) {
                    System.err.println("Access Denied for destination: " + destination);
                    throw new AccessDeniedException("Access Denied: Passenger role required");
                }
                if (destination.startsWith("/app/admin/") && authentication.get().getAuthorities().stream()
                        .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                    System.err.println("Access Denied for destination: " + destination);
                    throw new AccessDeniedException("Access Denied: Admin role required");
                }
            }

            return new AuthorizationDecision(authentication.get().isAuthenticated());
        };
    }

}
