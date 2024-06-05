package me.vrishab.auction.item;

import me.vrishab.auction.auction.AuctionRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class SpringDataJpaApplicationTests {

    @Autowired
    private AuctionRepository auctionRepo;


    /*
        Uncomment the setUp and tearDown if not using CommandLineRunner to initialize the DB
        Otherwise the integration and authorized endpoint test will not pass
    */

    @BeforeAll
    void setUp() {
//        Auction auction = new Auction();
//
//        auction.setName("auction");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.HOUR_OF_DAY, 1);
//        auction.setStartTime(calendar.getTime().toInstant());
//        calendar.add(Calendar.HOUR_OF_DAY, 1);
//        auction.setEndTime(calendar.getTime().toInstant());
//        auction.setInitialPrice(100.00);
//        auction.setBuyer("vr@domain.tld");
//        auction.setItems(generateItems());
//
//        auctionRepo.save(auction);
    }

//    private static Set<Item> generateItems() {
//        Set<Item> items = new HashSet<>();
//
//        for (int i = 0; i < 10; i++) {
//            Item item = new Item();
//            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
//            item.setDescription("Description " + i);
//            item.setLocation(i % 3 == 1 ? "MA" : "CA");
//            item.setImageUrls(Set.of("<images>"));
//            item.setExtras(null);
//            item.setLegitimacyProof("Proof");
//            item.setSeller("vr@domain.tld");
//            items.add(item);
//        }
//
////        items.forEach(System.out::println);
//
//        return items;
//    }

    @AfterAll
    void tearDown() {
        auctionRepo.deleteAll();
    }
}
