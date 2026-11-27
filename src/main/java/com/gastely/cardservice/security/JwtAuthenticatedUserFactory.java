package com.gastely.cardservice.security;

import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticatedUserFactory {

    public AuthenticatedUser create(Jwt jwt) {
        return new AuthenticatedUser(
                getRequiredUuidClaim(jwt, "tenantId"),
                getRequiredUuidClaim(jwt, "userId")
        );
    }

    private UUID getRequiredUuidClaim(Jwt jwt, String claimName) {
        String claimValue = jwt.getClaimAsString(claimName);

        if (claimValue == null || claimValue.isBlank()) {
            throw new IllegalArgumentException("JWT claim '%s' is required".formatted(claimName));
        }

        try {
            return UUID.fromString(claimValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWT claim '%s' must be a UUID".formatted(claimName), exception);
        }
    }
}
