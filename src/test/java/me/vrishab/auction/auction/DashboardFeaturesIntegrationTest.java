package me.vrishab.auction.auction;

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
    private UserRepository userRepository;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private User testUser;

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();
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
        auctionRepository.save(activeAuction);

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
                        .param("status", "ACTIVE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Active Auction"));

        // Find scheduled only
        mockMvc.perform(get(baseUrl + "/auctions")
                        .param("status", "SCHEDULED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Future Auction"));

        // Find ended only
        mockMvc.perform(get(baseUrl + "/auctions")
                        .param("status", "ENDED")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Past Auction"));
    }
}
