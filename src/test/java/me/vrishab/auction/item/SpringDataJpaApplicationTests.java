package me.vrishab.auction.item;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.AuctionRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class SpringDataJpaApplicationTests {

    @Autowired
    private AuctionRepository auctionRepo;


    @BeforeAll
    void setUp() {
        Auction auction = new Auction();

        auction.setName("auction");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setEndTime(calendar.getTime());
        auction.setInitialPrice(100.00);
        auction.setBuyer("vr@domain.tld");
        auction.setItems(generateItems());

        auctionRepo.save(auction);
    }

    private static Set<Item> generateItems() {
        Set<Item> items = new HashSet<>();

        for(int i=0; i<10; i++) {
            Item item = new Item();
            item.setName("Item "+i + (i%2 == 0 ? " (special)": ""));
            item.setDescription("Description "+i);
            item.setLocation("Location "+i);
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setLegitimacyProof("Proof");
            item.setSeller("vr@domain.tld");
            items.add(item);
        }

//        items.forEach(System.out::println);

        return items;
    }

    @AfterAll
    void tearDown() {
        auctionRepo.deleteAll();
    }
}