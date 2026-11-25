package com.gastely.cardservice.card.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gastely.cardservice.card.api.dto.CardResponse;
import com.gastely.cardservice.card.api.dto.CreateCardRequest;
import com.gastely.cardservice.card.api.dto.UpdateCardRequest;
import com.gastely.cardservice.card.api.dto.UpdateCardStatusRequest;
import com.gastely.cardservice.card.domain.Card;
import com.gastely.cardservice.card.domain.CardStatus;
import com.gastely.cardservice.card.repository.CardRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    @Transactional
    public CardResponse create(UUID tenantId, UUID ownerId, CreateCardRequest request) {
        String financialInstitution = request.financialInstitution().trim();
        String nickname = normalizeOptionalText(request.nickname());

        ensureCardDoesNotExist(tenantId, ownerId, financialInstitution, request.lastFourDigits());

        Card card = Card.create(
                tenantId,
                ownerId,
                nickname,
                financialInstitution,
                request.brand(),
                request.lastFourDigits(),
                request.creditLimit(),
                request.closingDay(),
                request.dueDay(),
                request.favorite()
        );

        return toResponse(cardRepository.save(card));
    }

    public CardResponse findById(UUID tenantId, UUID ownerId, UUID cardId) {
        return toResponse(findCard(tenantId, ownerId, cardId));
    }

    public Page<CardResponse> findAll(UUID tenantId, UUID ownerId, Pageable pageable) {
        return cardRepository.findAllByTenantIdAndOwnerId(tenantId, ownerId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CardResponse update(UUID tenantId, UUID ownerId, UUID cardId, UpdateCardRequest request) {
        Card card = findCard(tenantId, ownerId, cardId);
        String financialInstitution = request.financialInstitution().trim();
        String nickname = normalizeOptionalText(request.nickname());

        ensureUpdatedCardDoesNotDuplicate(tenantId, ownerId, financialInstitution, request.lastFourDigits(), cardId);

        card.update(
                nickname,
                financialInstitution,
                request.brand(),
                request.lastFourDigits(),
                request.creditLimit(),
                request.closingDay(),
                request.dueDay(),
                request.favorite()
        );

        return toResponse(card);
    }

    @Transactional
    public CardResponse changeStatus(
            UUID tenantId,
            UUID ownerId,
            UUID cardId,
            UpdateCardStatusRequest request
    ) {
        Card card = findCard(tenantId, ownerId, cardId);
        card.changeStatus(request.status());
        return toResponse(card);
    }

    private Card findCard(UUID tenantId, UUID ownerId, UUID cardId) {
        return cardRepository.findByIdAndTenantIdAndOwnerId(cardId, tenantId, ownerId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private void ensureCardDoesNotExist(
            UUID tenantId,
            UUID ownerId,
            String financialInstitution,
            String lastFourDigits
    ) {
        if (lastFourDigits != null
                && cardRepository.existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndStatusNot(
                        tenantId,
                        ownerId,
                        financialInstitution,
                        lastFourDigits,
                        CardStatus.CANCELED
                )) {
            throw new DuplicateCardException();
        }
    }

    private void ensureUpdatedCardDoesNotDuplicate(
            UUID tenantId,
            UUID ownerId,
            String financialInstitution,
            String lastFourDigits,
            UUID cardId
    ) {
        if (lastFourDigits != null
                && cardRepository.existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndIdNotAndStatusNot(
                        tenantId,
                        ownerId,
                        financialInstitution,
                        lastFourDigits,
                        cardId,
                        CardStatus.CANCELED
                )) {
            throw new DuplicateCardException();
        }
    }

    private String normalizeOptionalText(String value) {
        return value == null ? null : value.trim();
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getNickname(),
                card.getFinancialInstitution(),
                card.getBrand(),
                card.getLastFourDigits(),
                card.getCreditLimit(),
                card.getClosingDay(),
                card.getDueDay(),
                card.getStatus(),
                card.isFavorite(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
