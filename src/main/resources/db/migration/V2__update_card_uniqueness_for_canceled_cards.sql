DROP INDEX ux_cards_owner_institution_last_four_digits;

CREATE UNIQUE INDEX ux_cards_owner_institution_last_four_digits
    ON cards (
        tenant_id,
        owner_id,
        lower(btrim(financial_institution)),
        last_four_digits
    )
    WHERE last_four_digits IS NOT NULL
      AND status <> 'CANCELED';
