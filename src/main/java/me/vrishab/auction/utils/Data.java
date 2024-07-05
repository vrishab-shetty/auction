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

    public static Set<User> generateUsers(int n) {

        Set<User> users = new HashSet<>();

        for (int i = 0; i < n; i++) {

            String suffix = " " + i;
            User user = generateUser();
            Address homeAddress = user.getHomeAddress();

            homeAddress.setStreet("Street" + suffix);
            homeAddress.setCity("City" + suffix);
            homeAddress.setZipcode(new USZipcode("0221" + i));
            homeAddress.setCountry("Country" + suffix);

            user.setName("User" + suffix);
            user.setDescription("Description" + suffix);
            user.setEmail("name" + i + "@domain.tld");
            user.setHomeAddress(homeAddress);
            users.add(user);
        }

        return users;
    }

    public static User generateUser() {
        Address homeAddress = new Address(
                "Street",
                new USZipcode("02211"),
                "City",
                "Country"
        );

        User user = new User();
        user.setName("User");
        user.setPassword("password");
        user.setDescription("Description");
        user.setEnabled(true);
        user.setEmail("name@domain.tld");
        user.setContact("1234567890");
        user.setHomeAddress(homeAddress);

        return user;
    }

    public static BankAccount getBankAccount(int i) {
        BankAccount account = getBankAccount();

        account.setOwner("B-Owner " + i);
        account.setAccount("1234" + i);
        account.setSwift("BANKXY1234" + i);

        return account;
    }

    public static BankAccount getBankAccount() {
        return new BankAccount(
                "B-Owner", "1234", "Bank Name", "BANKXY12345"
        );
    }

    public static CreditCard getCreditCard() {

        String creditCardNo = creditCardNos.get(0);

        return new CreditCard(
                "Owner", creditCardNo, "06", "2016"
        );
    }

    public static CreditCard getCreditCard(int i) {

        String creditCardNo = creditCardNos.get(i % creditCardNos.size());

        CreditCard card = getCreditCard();

        card.setOwner("Owner " + i);
        card.setCardNumber(creditCardNo);
        card.setExpYear("201" + i);

        return card;
    }

    public static BillingDetails getRandomBillingDetails(int i) {
        Random rand = new Random();

        return (rand.nextBoolean()) ? getBankAccount(i) : getCreditCard(i);
    }


    public static Set<Item> generateItems(int n) {
        Set<Item> items = new HashSet<>();

        for (int i = 0; i < n; i++) {
            Item item = generateItem();

            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
            item.setDescription("Description " + i);
            item.setLocation(i % 3 == 1 ? "MA" : "CA");
            items.add(item);
        }

        return items;
    }

    public static Item generateItem() {

        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("Location");
        item.setImageUrls(Set.of("<images>"));
        item.setExtras(null);
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setLegitimacyProof("Proof");

        return item;
    }

    public static Set<Auction> generateAuctions(Set<Item> itemSet) {
        Set<Auction> auctions = new HashSet<>();

        Iterator<Item> items = itemSet.iterator();

        for (int i = 0; i < 3; i++) {
            Set<Item> auctionItems = new HashSet<>();

            for (int j = 0; j < 3; j++) {
                auctionItems.add(items.next());
            }

            Auction auction = generateAuction(auctionItems);
            auction.setName("Auction " + i);
            auctions.add(auction);
        }

        return auctions;
    }

    public static Auction generateAuction(Set<Item> auctionItems) {

        Auction auction = new Auction();

        auction.setName("Auction");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        auction.setStartTime(calendar.getTime().toInstant().minus(1, ChronoUnit.HOURS));
        auction.setEndTime(calendar.getTime().toInstant().plus(2, ChronoUnit.HOURS));
        auction.addAllItems(auctionItems);

        return auction;
    }
}
