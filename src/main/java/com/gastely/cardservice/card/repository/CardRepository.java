package com.gastely.cardservice.card.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gastely.cardservice.card.domain.Card;

public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByIdAndTenantIdAndOwnerId(UUID id, UUID tenantId, UUID ownerId);

    Page<Card> findAllByTenantIdAndOwnerId(UUID tenantId, UUID ownerId, Pageable pageable);

    boolean existsByTenantIdAndOwnerIdAndFinancialInstitutionIgnoreCaseAndLastFourDigits(
            UUID tenantId,
            UUID ownerId,
            String financialInstitution,
            String lastFourDigits
    );
}
