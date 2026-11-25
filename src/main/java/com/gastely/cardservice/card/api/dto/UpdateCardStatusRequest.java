package com.gastely.cardservice.card.api.dto;

import com.gastely.cardservice.card.domain.CardStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateCardStatusRequest(
        @NotNull
        CardStatus status
) {
}
