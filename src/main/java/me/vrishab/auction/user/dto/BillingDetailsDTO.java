package me.vrishab.auction.user.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreditCardDTO.class, name = "CARD"),
        @JsonSubTypes.Type(value = BankAccountDTO.class, name = "BANK_ACCOUNT")
})
public interface BillingDetailsDTO {
    UUID id();
    String owner();
    String type();
}
