package com.gastely.cardservice.card.domain;

public class InvalidCardStatusTransitionException extends RuntimeException {

    public InvalidCardStatusTransitionException(CardStatus currentStatus, CardStatus requestedStatus) {
        super("Invalid card status transition from %s to %s".formatted(currentStatus, requestedStatus));
    }
}
