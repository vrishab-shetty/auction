package me.vrishab.auction.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public abstract class BillingDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User user;

    @Column(nullable = false)
    private String owner;

    public BillingDetails(String owner) {
        this.owner = owner;
    }

    public abstract void pay(BigDecimal amount);
}
