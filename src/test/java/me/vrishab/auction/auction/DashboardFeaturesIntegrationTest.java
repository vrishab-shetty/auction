package me.vrishab.auction.auction;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DashboardFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private User testUser;

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("testuser@domain.tld");
        testUser.setPassword("password");
        testUser.setContact("1234567890");
        testUser.setEnabled(true);
        testUser.setHomeAddress(new me.vrishab.auction.user.model.Address("Street", new me.vrishab.auction.user.model.USZipcode("02215"), "Boston", "USA"));
        testUser = userRepository.save(testUser);

        // Active Auction
        Auction activeAuction = new Auction();
        activeAuction.setName("Active Auction");
        activeAuction.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        activeAuction.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        activeAuction.setUser(testUser);
        activeAuction = auctionRepository.save(activeAuction);

        // Future Auction
        Auction futureAuction = new Auction();
        futureAuction.setName("Future Auction");
        futureAuction.setStartTime(Instant.now().plus(1, ChronoUnit.HOURS));
        futureAuction.setEndTime(Instant.now().plus(2, ChronoUnit.HOURS));
        futureAuction.setUser(testUser);
        auctionRepository.save(futureAuction);

        // Past Auction
        Auction pastAuction = new Auction();
        pastAuction.setName("Past Auction");
        pastAuction.setStartTime(Instant.now().minus(2, ChronoUnit.HOURS));
        pastAuction.setEndTime(Instant.now().minus(1, ChronoUnit.HOURS));
        pastAuction.setUser(testUser);
        auctionRepository.save(pastAuction);

        // Items for Popularity
        Item item1 = new Item();
        item1.setName("Popular Item");
        item1.setDescription("Description");
        item1.setLocation("Boston");
        item1.setInitialPrice(BigDecimal.valueOf(100));
        item1.setAuction(activeAuction);
        item1 = itemRepository.save(item1);

        testUser.addFavouriteItem(item1);
        testUser = userRepository.save(testUser);

        Item item2 = new Item();
        item2.setName("Unpopular Item");
        item2.setDescription("Description");
        item2.setLocation("New York");
        item2.setInitialPrice(BigDecimal.valueOf(50));
        item2.setAuction(activeAuction);
        itemRepository.save(item2);
    }

    @Test
    @DisplayName("Verify active auctions filtering")
    void testActiveAuctionsFiltering() throws Exception {
        // Find all (active not provided)
        mockMvc.perform(get(baseUrl + "/auctions")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)));

        // Find active only
        mockMvc.perform(get(baseUrl + "/auctions")
                        .param("active", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Active Auction"));

        // Find inactive only
        mockMvc.perform(get(baseUrl + "/auctions")
                        .param("active", "false")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[*].name", containsInAnyOrder("Future Auction", "Past Auction")));
    }

    @Test
    @DisplayName("Verify popular items sorted by likes")
    void testPopularItemsSorting() throws Exception {
        mockMvc.perform(get(baseUrl + "/items/popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].name").value("Popular Item"))
                .andExpect(jsonPath("$.data.content[1].name").value("Unpopular Item"));
    }
}
