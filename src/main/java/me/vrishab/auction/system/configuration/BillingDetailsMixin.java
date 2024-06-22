package me.vrishab.auction.system.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import me.vrishab.auction.user.model.BankAccount;
import me.vrishab.auction.user.model.CreditCard;
import me.vrishab.auction.user.model.User;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreditCard.class, name = "creditCard"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "bankAccount")
})
public abstract class BillingDetailsMixin {
    @JsonIgnore
    public User user;
}