package me.vrishab.auction.user.converter;

import me.vrishab.auction.user.dto.BankAccountDTO;
import me.vrishab.auction.user.dto.BillingDetailsDTO;
import me.vrishab.auction.user.dto.CreditCardDTO;
import me.vrishab.auction.user.model.BankAccount;
import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.CreditCard;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BillingDetailsToBillingDetailsDTOConverter implements Converter<BillingDetails, BillingDetailsDTO> {

    @Override
    public BillingDetailsDTO convert(BillingDetails source) {
        if (source instanceof CreditCard creditCard) {
            return new CreditCardDTO(
                    creditCard.getId(),
                    creditCard.getOwner(),
                    creditCard.getCardNumber(),
                    creditCard.getExpMonth(),
                    creditCard.getExpYear()
            );
        } else if (source instanceof BankAccount bankAccount) {
            return new BankAccountDTO(
                    bankAccount.getId(),
                    bankAccount.getOwner(),
                    bankAccount.getAccount(),
                    bankAccount.getBankname(),
                    bankAccount.getSwift()
            );
        }
        return null;
    }
}
