CREATE TABLE cards (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    nickname VARCHAR(100),
    financial_institution VARCHAR(120) NOT NULL,
    brand VARCHAR(30) NOT NULL,
    last_four_digits VARCHAR(4),
    credit_limit NUMERIC(19, 2) NOT NULL,
    closing_day SMALLINT NOT NULL,
    due_day SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_cards_nickname_not_blank
        CHECK (nickname IS NULL OR char_length(btrim(nickname)) > 0),
    CONSTRAINT ck_cards_financial_institution_not_blank
        CHECK (char_length(btrim(financial_institution)) > 0),
    CONSTRAINT ck_cards_brand
        CHECK (brand IN ('VISA', 'MASTERCARD', 'ELO', 'AMERICAN_EXPRESS', 'HIPERCARD', 'DINERS_CLUB', 'OTHER')),
    CONSTRAINT ck_cards_last_four_digits
        CHECK (last_four_digits IS NULL OR last_four_digits ~ '^[0-9]{4}$'),
    CONSTRAINT ck_cards_credit_limit_positive
        CHECK (credit_limit > 0),
    CONSTRAINT ck_cards_closing_day
        CHECK (closing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_cards_due_day
        CHECK (due_day BETWEEN 1 AND 31),
    CONSTRAINT ck_cards_status
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'INACTIVE', 'CANCELED'))
);

CREATE UNIQUE INDEX ux_cards_owner_institution_last_four_digits
    ON cards (
        tenant_id,
        owner_id,
        lower(btrim(financial_institution)),
        last_four_digits
    )
    WHERE last_four_digits IS NOT NULL;

CREATE INDEX idx_cards_tenant_owner
    ON cards (tenant_id, owner_id);

CREATE INDEX idx_cards_tenant_owner_status
    ON cards (tenant_id, owner_id, status);
