package me.vrishab.auction;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.*;
import me.vrishab.auction.utils.Data;

import java.math.BigDecimal;
import java.util.*;

public class TestData {

    public static List<Item> generateItems() {

        List<Item> items = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a" + i));
            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
            item.setDescription("Description " + i);
            item.setLocation(i % 3 == 1 ? "MA" : "CA");
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b" + i % 2));
            item.setLegitimacyProof("Proof");
            item.setSeller("vr@domain.tld");
            items.add(item);
        }

        return items;
    }

    public static List<User> generateUsers() {

        List<User> users = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Address homeAddress = new Address(
                    "Street " + i,
                    new USZipcode("0221" + i),
                    "City " + i,
                    "Country " + i
            );

            User user = new User();
            user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa27" + i));
            user.setName("Name " + i);
            user.setPassword("password");
            user.setDescription("Description " + i);
            user.setEnabled(true);
            user.setEmail("name" + i + "@domain.tld");
            user.setContact("1234567890");
            user.setHomeAddress(homeAddress);

            if ((i + 1) % 2 == 0) {
                user.addBillingDetail(getBankAccount(i));
            } else {
                user.addBillingDetail(getCreditCard(i));
            }

            users.add(user);
        }

        return users;
    }


    public static List<Auction> generateAuctions(User user) {
        List<Auction> auctions = new ArrayList<>();

        Iterator<Item> items = generateItems().iterator();

        for (int i = 0; i < 9; i++) {
            Set<Item> auctionItems = new HashSet<>();

            auctionItems.add(items.next());

            Auction auction = new Auction();

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 2);
            auction.setStartTime(calendar.getTime().toInstant());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 3);
            auction.setEndTime(calendar.getTime().toInstant());
            auction.setInitialPrice(BigDecimal.valueOf(100.00));
            auction.setCurrentBid(BigDecimal.valueOf(150.00));
            auction.setBuyer("name1@domain.tld");
            auction.setItems(auctionItems);
            auctions.add(auction);

            user.addAuction(auction);

        }

        return auctions;
    }

    public static BankAccount getBankAccount(int i) {
        return Data.getBankAccount(i);
    }

    public static CreditCard getCreditCard(int i) {
        return Data.getCreditCard(i);
    }
}
