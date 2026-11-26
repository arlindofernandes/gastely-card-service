package com.gastely.cardservice.card.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CardTest {

    @ParameterizedTest(name = "{0} -> {1} should be allowed: {2}")
    @MethodSource("statusTransitions")
    void shouldApplyAllStatusTransitions(CardStatus currentStatus, CardStatus requestedStatus, boolean allowed) {
        Card card = cardWithStatus(currentStatus);

        if (allowed) {
            assertDoesNotThrow(() -> card.changeStatus(requestedStatus));
            assertEquals(requestedStatus, card.getStatus());
        } else {
            assertThrows(InvalidCardStatusTransitionException.class, () -> card.changeStatus(requestedStatus));
            assertEquals(currentStatus, card.getStatus());
        }
    }

    @ParameterizedTest
    @MethodSource("statusTransitions")
    void shouldPreserveTimestampsForIdempotentStatusChanges(
            CardStatus currentStatus,
            CardStatus requestedStatus,
            boolean allowed
    ) {
        if (currentStatus != requestedStatus) {
            return;
        }

        Card card = cardWithStatus(currentStatus);
        card.prePersist();
        OffsetDateTime originalUpdatedAt = card.getUpdatedAt();

        card.changeStatus(requestedStatus);

        assertEquals(originalUpdatedAt, card.getUpdatedAt());
    }

    private static Stream<Arguments> statusTransitions() {
        return Stream.of(
                Arguments.of(CardStatus.ACTIVE, CardStatus.ACTIVE, true),
                Arguments.of(CardStatus.ACTIVE, CardStatus.BLOCKED, true),
                Arguments.of(CardStatus.ACTIVE, CardStatus.INACTIVE, true),
                Arguments.of(CardStatus.ACTIVE, CardStatus.CANCELED, true),
                Arguments.of(CardStatus.BLOCKED, CardStatus.ACTIVE, true),
                Arguments.of(CardStatus.BLOCKED, CardStatus.BLOCKED, true),
                Arguments.of(CardStatus.BLOCKED, CardStatus.INACTIVE, true),
                Arguments.of(CardStatus.BLOCKED, CardStatus.CANCELED, true),
                Arguments.of(CardStatus.INACTIVE, CardStatus.ACTIVE, true),
                Arguments.of(CardStatus.INACTIVE, CardStatus.BLOCKED, false),
                Arguments.of(CardStatus.INACTIVE, CardStatus.INACTIVE, true),
                Arguments.of(CardStatus.INACTIVE, CardStatus.CANCELED, true),
                Arguments.of(CardStatus.CANCELED, CardStatus.ACTIVE, false),
                Arguments.of(CardStatus.CANCELED, CardStatus.BLOCKED, false),
                Arguments.of(CardStatus.CANCELED, CardStatus.INACTIVE, false),
                Arguments.of(CardStatus.CANCELED, CardStatus.CANCELED, true)
        );
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
