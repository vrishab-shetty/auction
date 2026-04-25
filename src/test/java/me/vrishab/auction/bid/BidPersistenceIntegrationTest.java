package me.vrishab.auction.bid;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.AuctionRepository;
import me.vrishab.auction.auction.AuctionService;
import me.vrishab.auction.auction.dto.BidRequestDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Data;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("Bid Persistence Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("integration")
@ActiveProfiles("integration_test")
public class BidPersistenceIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BidRepository bidRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private AuctionRepository auctionRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AuctionService auctionService;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private UUID auctionId;
    private UUID itemId;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        User owner = userRepo.findByEmail("name0@domain.tld").orElseThrow();

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
    @DisplayName("Sequential bids from multiple users persist as an ordered history")
    void testSequentialBidsPersistInOrder() throws Exception {
        String[] bidderEmails = {"name1@domain.tld", "name2@domain.tld", "name3@domain.tld"};
        BigDecimal[] amounts = {
                BigDecimal.valueOf(110.00),
                BigDecimal.valueOf(120.00),
                BigDecimal.valueOf(130.00)
        };

        for (int i = 0; i < bidderEmails.length; i++) {
            placeBid(bidderEmails[i], amounts[i]);
            // Ensure distinct placed_at timestamps for deterministic ordering.
            Thread.sleep(10);
        }

        Page<Bid> history = bidRepo.findByItemIdOrderByPlacedAtDesc(itemId, PageRequest.of(0, 10));
        List<Bid> bids = history.getContent();

        assertThat(bids).hasSize(3);
        // Reverse chronological order: newest (130.00) first.
        assertThat(bids.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(130.00));
        assertThat(bids.get(1).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(120.00));
        assertThat(bids.get(2).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(110.00));

        assertThat(bids.get(0).getBidder().getEmail()).isEqualTo("name3@domain.tld");
        assertThat(bids.get(1).getBidder().getEmail()).isEqualTo("name2@domain.tld");
        assertThat(bids.get(2).getBidder().getEmail()).isEqualTo("name1@domain.tld");

        bids.forEach(b -> {
            assertThat(b.getItem().getId()).isEqualTo(itemId);
            assertThat(b.getPlacedAt()).isNotNull();
        });

        // Current state should match the latest bid.
        Item finalItem = itemRepo.findById(itemId).orElseThrow();
        assertThat(finalItem.getCurrentBid()).isEqualByComparingTo(BigDecimal.valueOf(130.00));
        assertThat(finalItem.getBuyer().getEmail()).isEqualTo("name3@domain.tld");
    }

    @Test
    @DisplayName("Failed item save rolls back the bid row — transactional atomicity")
    void testBidRollbackOnItemSaveFailure() throws Exception {
        MethodInterceptor interceptor = invocation -> {
            if ("save".equals(invocation.getMethod().getName())) {
                throw new RuntimeException("Simulated Database Failure");
            }
            return invocation.proceed();
        };

        ProxyFactory factory = new ProxyFactory(itemRepo);
        factory.addAdvice(interceptor);
        ItemRepository proxiedRepo = (ItemRepository) factory.getProxy();
        ItemRepository originalRepo = (ItemRepository) ReflectionTestUtils.getField(auctionService, "itemRepo");
        ReflectionTestUtils.setField(auctionService, "itemRepo", proxiedRepo);

        try {
            BidRequestDTO bidRequest = new BidRequestDTO(BigDecimal.valueOf(500.00));
            String token = login("name1@domain.tld");

            mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                            .header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bidRequest)))
                    .andExpect(status().isServiceUnavailable());

            Page<Bid> bids = bidRepo.findByItemIdOrderByPlacedAtDesc(itemId, PageRequest.of(0, 10));
            assertThat(bids.getContent()).isEmpty();
        } finally {
            ReflectionTestUtils.setField(auctionService, "itemRepo", originalRepo);
        }
    }

    private void placeBid(String email, BigDecimal amount) throws Exception {
        String token = login(email);
        BidRequestDTO bidRequest = new BidRequestDTO(amount);
        mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isOk());
    }

    private String login(String email) throws Exception {
        MvcResult authResult = mockMvc.perform(post(baseUrl + "/users/login")
                        .with(httpBasic(email, "password")))
                .andExpect(status().isOk())
                .andReturn();
        String response = authResult.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("data").get("token").asText();
    }
}
