package com.gastely.cardservice.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthenticatedUserFactoryTest {

    private final JwtAuthenticatedUserFactory factory = new JwtAuthenticatedUserFactory();

    @Test
    void shouldCreateAuthenticatedUserFromValidClaims() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser = factory.create(jwt(tenantId.toString(), ownerId.toString()));

        assertEquals(tenantId, authenticatedUser.tenantId());
        assertEquals(ownerId, authenticatedUser.ownerId());
    }

    @Test
    void shouldRejectMissingUserIdClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("tenantId", UUID.randomUUID().toString())
                .build();

        assertThrows(IllegalArgumentException.class, () -> factory.create(jwt));
    }

    @Test
    void shouldRejectInvalidTenantIdClaim() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(jwt("invalid-tenant-id", UUID.randomUUID().toString())));
    }

    private Jwt jwt(String tenantId, String ownerId) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("tenantId", tenantId)
                .claim("userId", ownerId)
                .build();
    }
}
