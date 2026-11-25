package com.gastely.cardservice.card.service;

public class DuplicateCardException extends RuntimeException {

    public DuplicateCardException() {
        super("A card with the same financial institution and last four digits already exists");
    }
}
