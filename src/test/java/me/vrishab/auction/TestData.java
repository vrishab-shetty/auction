package me.vrishab.auction;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.Address;
import me.vrishab.auction.user.model.USZipcode;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Data;

import java.time.temporal.ChronoUnit;
import java.util.*;

public class TestData {

    public static List<Item> generateItems(User seller, User buyer) {

        List<Item> items = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Item item = Data.generateItem();

            item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a" + i));
            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
            item.setDescription("Description " + i);
            item.setLocation(i % 3 == 1 ? "MA" : "CA");
            item.setLegitimacyProof("Proof");
            item.setSeller(seller.getEmail());
            item.setBuyer(buyer);

            items.add(item);
        }

        return items;
    }

    public static List<User> generateUsers() {

        List<User> users = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            String suffix = " " + i;
            User user = Data.generateUser();
            Address homeAddress = user.getHomeAddress();

            homeAddress.setStreet("Street" + suffix);
            homeAddress.setCity("City" + suffix);
            homeAddress.setZipcode(new USZipcode("0221" + i));
            homeAddress.setCountry("Country" + suffix);

            user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa27" + i));
            user.setName("User" + suffix);
            user.setDescription("Description" + suffix);
            user.setEmail("name" + i + "@domain.tld");
            user.setHomeAddress(homeAddress);
            users.add(user);

        }

        return users;
    }


    public static List<Auction> generateAuctions(User seller, User buyer) {

        Iterator<Item> items = generateItems(seller, buyer).iterator();

        List<Auction> auctions = new ArrayList<>();

        for (int i = 0; i < 9; i++) {

            Auction auction = Data.generateAuction(Set.of(items.next()));

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            auction.setStartTime(calendar.getTime().toInstant().plus(1 + i % 2, ChronoUnit.HOURS));
            auction.setEndTime(calendar.getTime().toInstant().plus(2 + i % 3, ChronoUnit.HOURS));
            auction.setUser(seller);

            auctions.add(auction);

        }

        return auctions;
    }

    public static User generateUser() {
        User user = Data.generateUser();

        user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));

        return user;
    }
}
