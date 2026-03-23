package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.BidRequestDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Concurrency Integration Tests for Bidding")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("integration")
@ActiveProfiles("integration_test")
public class ConcurrencyIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private AuctionRepository auctionRepo;

    @Autowired
    private UserRepository userRepo;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String token;
    private UUID auctionId;
    private UUID itemId;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Get Authentication Token
        MvcResult authResult = this.mockMvc.perform(post(baseUrl + "/users/login")
                .with(httpBasic("name1@domain.tld", "password")))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = authResult.getResponse().getContentAsString();
        this.token = "Bearer " + objectMapper.readTree(response).get("data").get("token").asText();

        // 2. Setup Test Auction and Item (not owned by name1@domain.tld)
        User owner = userRepo.findByEmail("name2@domain.tld").orElseThrow();
        Item item = Data.generateItem();
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        
        Auction auction = Data.generateAuction(Set.of(item));
        auction.setUser(owner);
        auction.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        auction.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        
        Auction savedAuction = auctionRepo.save(auction);
        this.auctionId = savedAuction.getId();
        this.itemId = savedAuction.getItems().iterator().next().getId();
    }

    @Test
    @DisplayName("Should handle 10 simultaneous bids atomically")
    void testConcurrentBiddingAtomicUpdates() throws Exception {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<BigDecimal> attemptedBids = new ArrayList<>();

        for (int i = 1; i <= numberOfThreads; i++) {
            BigDecimal bidAmount = BigDecimal.valueOf(100.00 + i);
            attemptedBids.add(bidAmount);
            
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for signal to start all at once
                    
                    BidRequestDTO bidRequest = new BidRequestDTO(bidAmount);
                    MvcResult result = mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bidRequest)))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else if (status == 409 || status == 503) {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads
        doneLatch.await(10, TimeUnit.SECONDS); // Wait for completion
        executorService.shutdown();

        // VALIDATION
        Item finalItem = itemRepo.findById(itemId).orElseThrow();
        
        System.out.println("Success count: " + successCount.get());
        System.out.println("Conflict count: " + conflictCount.get());
        System.out.println("Final Price: " + finalItem.getCurrentBid());

        // At least one must succeed, the rest should fail gracefully due to the lock
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(numberOfThreads);
        
        // Final price in DB must be one of the successfully processed bids
        assertThat(finalItem.getCurrentBid()).isNotNull();
        boolean found = attemptedBids.stream()
                .anyMatch(bid -> bid.compareTo(finalItem.getCurrentBid()) == 0);
        assertThat(found).isTrue();
    }
}
