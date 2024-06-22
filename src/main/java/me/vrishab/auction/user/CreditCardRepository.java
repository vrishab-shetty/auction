package me.vrishab.auction.user;

import me.vrishab.auction.user.model.CreditCard;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends BillingDetailsRepository<CreditCard, Long> {
    List<CreditCard> findByCardNumber(String cardNumber);
}