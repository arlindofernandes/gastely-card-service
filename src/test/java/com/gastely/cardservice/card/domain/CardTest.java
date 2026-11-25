package com.gastely.cardservice.card.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void shouldAllowAllConfiguredTransitions() {
        assertTransition(CardStatus.ACTIVE, CardStatus.BLOCKED);
        assertTransition(CardStatus.ACTIVE, CardStatus.INACTIVE);
        assertTransition(CardStatus.ACTIVE, CardStatus.CANCELED);
        assertTransition(CardStatus.BLOCKED, CardStatus.ACTIVE);
        assertTransition(CardStatus.BLOCKED, CardStatus.INACTIVE);
        assertTransition(CardStatus.BLOCKED, CardStatus.CANCELED);
        assertTransition(CardStatus.INACTIVE, CardStatus.ACTIVE);
        assertTransition(CardStatus.INACTIVE, CardStatus.CANCELED);
    }

    @Test
    void shouldTreatSameStatusAsIdempotent() {
        for (CardStatus status : CardStatus.values()) {
            Card card = cardWithStatus(status);

            assertDoesNotThrow(() -> card.changeStatus(status));
        }
    }

    @Test
    void shouldRejectAllForbiddenTransitions() {
        assertInvalidTransition(CardStatus.INACTIVE, CardStatus.BLOCKED);
        assertInvalidTransition(CardStatus.CANCELED, CardStatus.ACTIVE);
        assertInvalidTransition(CardStatus.CANCELED, CardStatus.BLOCKED);
        assertInvalidTransition(CardStatus.CANCELED, CardStatus.INACTIVE);
    }

    private void assertTransition(CardStatus currentStatus, CardStatus requestedStatus) {
        Card card = cardWithStatus(currentStatus);

        assertDoesNotThrow(() -> card.changeStatus(requestedStatus));
    }

    private void assertInvalidTransition(CardStatus currentStatus, CardStatus requestedStatus) {
        Card card = cardWithStatus(currentStatus);

        assertThrows(InvalidCardStatusTransitionException.class, () -> card.changeStatus(requestedStatus));
    }

    private Card cardWithStatus(CardStatus status) {
        Card card = newCard();

        if (status != CardStatus.ACTIVE) {
            card.changeStatus(status);
        }

        return card;
    }

    private Card newCard() {
        return Card.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Main card",
                "Gastely Bank",
                CardBrand.VISA,
                "1234",
                new BigDecimal("1000.00"),
                (short) 10,
                (short) 20,
                false
        );
    }
}
