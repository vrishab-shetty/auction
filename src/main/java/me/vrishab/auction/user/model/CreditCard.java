package me.vrishab.auction.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

import java.math.BigDecimal;

@Entity
@PrimaryKeyJoinColumn(name = "CREDITCARD_ID")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class CreditCard extends BillingDetails {

    @Column(nullable = false)
    @CreditCardNumber(message = "Not a valid credit card number")
    @NotNull(message = "cardNumber is required")
    private String cardNumber;

    @Column(nullable = false)
    @Pattern(regexp = "^(0[1-9]|1[0-2])$",
            message = "Must be formatted MM")
    @NotNull(message = "expMonth is required")
    private String expMonth;

    @Column(nullable = false)
    @Pattern(regexp = "^([2-9][0-9][0-9][0-9])$",
            message = "Must be formatted YYYY")
    @NotNull(message = "expYear is required")
    private String expYear;

    public CreditCard(String owner, String cardNumber, String expMonth, String expYear) {
        super(owner);
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    @Override
    public void pay(BigDecimal amount) {

    }
}
