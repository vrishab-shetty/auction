package me.vrishab.auction;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.AuctionService;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.BillingDetailsRepository;
import me.vrishab.auction.user.UserService;
import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static me.vrishab.auction.utils.Data.*;
import static me.vrishab.auction.utils.Data.generateAuctions;

@Component
@Profile("integration_test")
public class DBInitializer implements CommandLineRunner {

    private final UserService userService;

    private final AuctionService auctionService;

    private final BillingDetailsRepository<BillingDetails, UUID> billingDetailsRepository;

    public DBInitializer(UserService userService, AuctionService auctionService, BillingDetailsRepository<BillingDetails, UUID> billingDetailsRepository) {
        this.userService = userService;
        this.auctionService = auctionService;
        this.billingDetailsRepository = billingDetailsRepository;
    }


    @Override
    public void run(String... args) {
        Set<User> userSet = generateUsers(9);

        userSet.forEach(userService::save);

        int i = 0;
        for (User user : userSet) {
            BillingDetails details = getRandomBillingDetails(i);
            details.setUser(user);
            billingDetailsRepository.save(details);
            i++;
        }

        Iterator<User> users = userSet.iterator();

        Set<Item> itemSet = generateItems(9);

        Set<Auction> auctions = generateAuctions(itemSet);

        for (Auction auction : auctions) {
            User user = users.next();
            auctionService.add(user.getId().toString(), auction);
        }

        Iterator<Item> items = itemSet.iterator();

        users.forEachRemaining(user -> {
            Item item = items.next();
            userService.addItem(user.getId().toString(), item.getId().toString());
        });

    }

}