package com.gastely.cardservice.card.domain;

public enum CardStatus {
    ACTIVE,
    BLOCKED,
    INACTIVE,
    CANCELED;

    public boolean canTransitionTo(CardStatus targetStatus) {
        return switch (this) {
            case ACTIVE -> targetStatus == BLOCKED
                    || targetStatus == INACTIVE
                    || targetStatus == CANCELED;
            case BLOCKED -> targetStatus == ACTIVE
                    || targetStatus == INACTIVE
                    || targetStatus == CANCELED;
            case INACTIVE -> targetStatus == ACTIVE
                    || targetStatus == CANCELED;
            case CANCELED -> false;
        };
    }
}
