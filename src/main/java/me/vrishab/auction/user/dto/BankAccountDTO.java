package me.vrishab.auction.user.dto;

import java.util.UUID;

public record BankAccountDTO(
        UUID id,
        String owner,
        String account,
        String bankname,
        String swift,
        String type
) implements BillingDetailsDTO {
    public BankAccountDTO(UUID id, String owner, String account, String bankname, String swift) {
        this(id, owner, account, bankname, swift, "BANK_ACCOUNT");
    }
}
