package com.gastely.cardservice.card.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(length = 100)
    private String nickname;

    @Column(name = "financial_institution", nullable = false, length = 120)
    private String financialInstitution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CardBrand brand;

    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "closing_day", nullable = false)
    private short closingDay;

    @Column(name = "due_day", nullable = false)
    private short dueDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    private Card(
            UUID tenantId,
            UUID ownerId,
            String nickname,
            String financialInstitution,
            CardBrand brand,
            String lastFourDigits,
            BigDecimal creditLimit,
            short closingDay,
            short dueDay,
            boolean favorite
    ) {
        this.tenantId = Objects.requireNonNull(tenantId);
        this.ownerId = Objects.requireNonNull(ownerId);
        update(nickname, financialInstitution, brand, lastFourDigits, creditLimit, closingDay, dueDay, favorite);
    }

    public static Card create(
            UUID tenantId,
            UUID ownerId,
            String nickname,
            String financialInstitution,
            CardBrand brand,
            String lastFourDigits,
            BigDecimal creditLimit,
            short closingDay,
            short dueDay,
            boolean favorite
    ) {
        return new Card(
                tenantId,
                ownerId,
                nickname,
                financialInstitution,
                brand,
                lastFourDigits,
                creditLimit,
                closingDay,
                dueDay,
                favorite
        );
    }

    public void update(
            String nickname,
            String financialInstitution,
            CardBrand brand,
            String lastFourDigits,
            BigDecimal creditLimit,
            short closingDay,
            short dueDay,
            boolean favorite
    ) {
        this.nickname = nickname;
        this.financialInstitution = Objects.requireNonNull(financialInstitution);
        this.brand = Objects.requireNonNull(brand);
        this.lastFourDigits = lastFourDigits;
        this.creditLimit = Objects.requireNonNull(creditLimit);
        this.closingDay = closingDay;
        this.dueDay = dueDay;
        this.favorite = favorite;
    }

    public void changeStatus(CardStatus newStatus) {
        Objects.requireNonNull(newStatus);

        if (status == newStatus) {
            return;
        }

        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidCardStatusTransitionException(status, newStatus);
        }

        status = newStatus;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
