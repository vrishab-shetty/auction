package me.vrishab.auction.utils;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.*;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class Data {

    private static final List<String> creditCardNos = List.of(
            "378282246310005",
            "371449635398431",
            "378734493671000",
            "5610591081018250",
            "30569309025904",
            "38520000023237",
            "6011111111111117",
            "6011000990139424",
            "3530111333300000",
            "3566002020360505",
            "5555555555554444",
            "5105105105105100",
            "4111111111111111",
            "4012888888881881"
    );

    public static Set<User> generateUsers() {

        Set<User> users = new HashSet<>();

        for (int i = 0; i < 9; i++) {

            Address homeAddress = new Address(
                    "Street " + i,
                    new USZipcode("0221" + i),
                    "City " + i,
                    "Country " + i
            );

            User user = new User();
            user.setName("User " + i);
            user.setPassword("password");
            user.setDescription("Description " + i);
            user.setEnabled(true);
            user.setEmail("name" + i + "@domain.tld");
            user.setContact("1234567890");
            user.setHomeAddress(homeAddress);

            if ((i + 1) % 2 == 0) {
                BankAccount bankAccount = getBankAccount(i);
                user.addBillingDetail(bankAccount);
            } else {
                CreditCard creditCard = getCreditCard(i);
                user.addBillingDetail(creditCard);
            }

            users.add(user);
        }

        return users;
    }

    public static BankAccount getBankAccount(int i) {
        return new BankAccount(
                "B-Owner " + i, "1234" + i, "Bank Name " + i, "BANKXY1234" + i
        );
    }

    public static CreditCard getCreditCard(int i) {
        return new CreditCard(
                "Owner " + i, creditCardNos.get(i), "06", "201" + i
        );
    }


    public static Set<Item> generateItems() {
        Set<Item> items = new HashSet<>();

        for (int i = 0; i < 9; i++) {
            Item item = new Item();
            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
            item.setDescription("Description " + i);
            item.setLocation(i % 3 == 1 ? "MA" : "CA");
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setLegitimacyProof("Proof");
            items.add(item);
        }

        return items;
    }

    public static Set<Auction> generateAuctions() {
        Set<Auction> auctions = new HashSet<>();

        Iterator<Item> items = generateItems().iterator();

        for (int i = 0; i < 3; i++) {
            Set<Item> auctionItems = new HashSet<>();

            for (int j = 0; j < 3; j++) {
                auctionItems.add(items.next());
            }

            Auction auction = new Auction();
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            auction.setStartTime(calendar.getTime().toInstant().minus(1, ChronoUnit.HOURS));
            auction.setEndTime(calendar.getTime().toInstant().plus(2, ChronoUnit.HOURS));
            auction.setInitialPrice(BigDecimal.valueOf(100.00));
            auction.setItems(auctionItems);

            auctions.add(auction);
        }

        return auctions;
    }
}
