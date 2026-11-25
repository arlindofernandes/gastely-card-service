package com.gastely.cardservice.card.service;

import java.util.UUID;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(UUID cardId) {
        super("Card not found: %s".formatted(cardId));
    }
}
