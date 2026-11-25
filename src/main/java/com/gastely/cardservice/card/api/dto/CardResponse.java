package com.gastely.cardservice.card.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.gastely.cardservice.card.domain.CardBrand;
import com.gastely.cardservice.card.domain.CardStatus;

public record CardResponse(
        UUID id,
        String nickname,
        String financialInstitution,
        CardBrand brand,
        String lastFourDigits,
        BigDecimal creditLimit,
        short closingDay,
        short dueDay,
        CardStatus status,
        boolean favorite,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
