package com.gastely.cardservice.card.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gastely.cardservice.card.api.dto.CardResponse;
import com.gastely.cardservice.card.api.dto.CreateCardRequest;
import com.gastely.cardservice.card.api.dto.UpdateCardRequest;
import com.gastely.cardservice.card.domain.Card;
import com.gastely.cardservice.card.domain.CardBrand;
import com.gastely.cardservice.card.domain.CardStatus;
import com.gastely.cardservice.card.repository.CardRepository;

import org.hibernate.exception.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void shouldNormalizeTextAndCreateCard() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        CreateCardRequest request = createRequest("  Main card  ", "  Nubank  ", "1234");

        when(cardRepository.existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndStatusNot(
                tenantId,
                ownerId,
                "Nubank",
                "1234",
                CardStatus.CANCELED
        )).thenReturn(false);
        when(cardRepository.saveAndFlush(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.create(tenantId, ownerId, request);

        assertEquals("Main card", response.nickname());
        assertEquals("Nubank", response.financialInstitution());
        verify(cardRepository).saveAndFlush(any(Card.class));
    }

    @Test
    void shouldRejectDuplicateCardBeforeSaving() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        CreateCardRequest request = createRequest("Main card", "Nubank", "1234");

        when(cardRepository.existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndStatusNot(
                tenantId,
                ownerId,
                "Nubank",
                "1234",
                CardStatus.CANCELED
        )).thenReturn(true);

        assertThrows(DuplicateCardException.class, () -> cardService.create(tenantId, ownerId, request));

        verify(cardRepository, never()).saveAndFlush(any(Card.class));
    }

    @Test
    void shouldNotFindCardOutsideOwnerScope() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findByIdAndTenantIdAndOwnerId(cardId, tenantId, ownerId))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.findById(tenantId, ownerId, cardId));

        verify(cardRepository).findByIdAndTenantIdAndOwnerId(eq(cardId), eq(tenantId), eq(ownerId));
    }

    @Test
    void shouldUseTenantAndOwnerScopeWhenListingCards() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);

        when(cardRepository.findAllByTenantIdAndOwnerId(tenantId, ownerId, pageable))
                .thenReturn(Page.empty(pageable));

        cardService.findAll(tenantId, ownerId, pageable);

        verify(cardRepository).findAllByTenantIdAndOwnerId(tenantId, ownerId, pageable);
    }

    @Test
    void shouldNotUpdateCardOutsideOwnerScope() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        when(cardRepository.findByIdAndTenantIdAndOwnerId(cardId, tenantId, ownerId))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.update(
                tenantId,
                ownerId,
                cardId,
                updateRequest()
        ));

        verify(cardRepository).findByIdAndTenantIdAndOwnerId(cardId, tenantId, ownerId);
        verify(cardRepository, never()).saveAndFlush(any(Card.class));
    }

    @Test
    void shouldTranslateOnlyCardUniqueConstraintViolations() {
        CreateCardRequest request = createRequest("Main card", "Nubank", "1234");
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "duplicate card",
                new SQLException("duplicate key", "23505"),
                "ux_cards_owner_institution_last_four_digits"
        );

        when(cardRepository.saveAndFlush(any(Card.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate card", constraintViolationException));

        assertThrows(DuplicateCardException.class, () -> cardService.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                request
        ));
    }

    @Test
    void shouldPropagateOtherIntegrityViolations() {
        CreateCardRequest request = createRequest("Main card", "Nubank", "1234");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("other integrity violation");

        when(cardRepository.saveAndFlush(any(Card.class))).thenThrow(exception);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class, () -> cardService.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                request
        ));

        assertEquals(exception, thrown);
    }

    private CreateCardRequest createRequest(String nickname, String financialInstitution, String lastFourDigits) {
        return new CreateCardRequest(
                nickname,
                financialInstitution,
                CardBrand.MASTERCARD,
                lastFourDigits,
                new BigDecimal("1000.00"),
                (short) 10,
                (short) 20,
                false
        );
    }

    private UpdateCardRequest updateRequest() {
        return new UpdateCardRequest(
                "Main card",
                "Nubank",
                CardBrand.MASTERCARD,
                "1234",
                new BigDecimal("1000.00"),
                (short) 10,
                (short) 20,
                false
        );
    }
}
