package me.vrishab.auction.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@PrimaryKeyJoinColumn(name = "BANKACCOUNT_ID")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class BankAccount extends BillingDetails {

    @Column(nullable = false)
    @NotBlank(message = "account is required")
    private String account;

    @Column(nullable = false)
    @NotBlank(message = "bankname is required")
    private String bankname;

    @Column(nullable = false)
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Please provide valid swift code")
    @NotBlank(message = "swift is required")
    private String swift;

    public BankAccount(String owner, String account, String bankname, String swift) {
        super(owner);
        this.account = account;
        this.bankname = bankname;
        this.swift = swift;
    }

    @Override
    public void pay(BigDecimal amount) {

    }
}
