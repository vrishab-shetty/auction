package me.vrishab.auction.item;

import me.vrishab.auction.TestData;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.AuctionRepository;
import me.vrishab.auction.auction.AuctionService;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.UserService;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Data;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.temporal.ChronoUnit;
import java.util.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
abstract class SpringDataJpaApplicationTests {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    @BeforeAll
    void setUp() {
        User user = Data.generateUser();

        userService.save(user);

        Set<Item> items = new HashSet<>(Data.generateItems(10));
        Auction auction = Data.generateAuction(items);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        auction.setStartTime(calendar.getTime().toInstant().plus(1, ChronoUnit.HOURS));
        auction.setEndTime(calendar.getTime().toInstant().plus(2, ChronoUnit.HOURS));

        auctionService.add(user.getId().toString(), auction);
    }


    @AfterAll
    void tearDown() {
        auctionRepo.deleteAll();
        userRepo.deleteAll();
    }
}
