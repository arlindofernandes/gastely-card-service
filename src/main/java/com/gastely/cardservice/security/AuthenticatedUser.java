package com.gastely.cardservice.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID tenantId,
        UUID ownerId
) {
}
