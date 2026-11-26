package com.gastely.cardservice.card.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.gastely.cardservice.card.domain.Card;
import com.gastely.cardservice.card.domain.CardBrand;
import com.gastely.cardservice.card.domain.CardStatus;

@SpringBootTest
@Transactional
class CardRepositoryIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Test
    void shouldRejectDuplicateActiveCards() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        cardRepository.saveAndFlush(newCard(tenantId, ownerId));

        assertThrows(DataIntegrityViolationException.class, () -> cardRepository.saveAndFlush(newCard(tenantId, ownerId)));
    }

    @Test
    void shouldAllowDuplicateWhenExistingCardIsCanceled() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Card canceledCard = cardRepository.saveAndFlush(newCard(tenantId, ownerId));
        canceledCard.changeStatus(CardStatus.CANCELED);
        cardRepository.saveAndFlush(canceledCard);

        assertDoesNotThrow(() -> cardRepository.saveAndFlush(newCard(tenantId, ownerId)));
    }

    @Test
    void shouldAllowSameCardDetailsForDifferentTenantOrOwner() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        cardRepository.saveAndFlush(newCard(tenantId, ownerId));

        assertDoesNotThrow(() -> cardRepository.saveAndFlush(newCard(UUID.randomUUID(), ownerId)));
        assertDoesNotThrow(() -> cardRepository.saveAndFlush(newCard(tenantId, UUID.randomUUID())));
    }

    private Card newCard(UUID tenantId, UUID ownerId) {
        return Card.create(
                tenantId,
                ownerId,
                "Primary card",
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
