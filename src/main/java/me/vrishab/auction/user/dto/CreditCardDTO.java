package me.vrishab.auction.user.dto;

import java.util.UUID;

public record CreditCardDTO(
        UUID id,
        String owner,
        String cardNumber,
        String expMonth,
        String expYear,
        String type
) implements BillingDetailsDTO {
    public CreditCardDTO(UUID id, String owner, String cardNumber, String expMonth, String expYear) {
        this(id, owner, cardNumber, expMonth, expYear, "CARD");
    }
}
