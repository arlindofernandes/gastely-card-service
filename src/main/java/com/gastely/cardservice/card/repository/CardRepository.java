package com.gastely.cardservice.card.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gastely.cardservice.card.domain.Card;
import com.gastely.cardservice.card.domain.CardStatus;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByIdAndTenantIdAndOwnerId(UUID id, UUID tenantId, UUID ownerId);

    Page<Card> findAllByTenantIdAndOwnerId(UUID tenantId, UUID ownerId, Pageable pageable);

    boolean existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndStatusNot(
            UUID tenantId,
            UUID ownerId,
            String financialInstitution,
            String lastFourDigits,
            CardStatus status
    );

    boolean existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigitsAndIdNotAndStatusNot(
            UUID tenantId,
            UUID ownerId,
            String financialInstitution,
            String lastFourDigits,
            UUID id,
            CardStatus status
    );
}
