package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepo;

    @Autowired
    private UserRepository userRepo;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private String token;
    private String userId;
    private UUID auctionId;
    private UUID itemId;

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() throws Exception {
        MvcResult authResult = this.mockMvc.perform(post(baseUrl + "/users/login")
                .with(httpBasic("name1@domain.tld", "password")))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = authResult.getResponse().getContentAsString();
        this.userId = objectMapper.readTree(response).get("data").get("userInfo").get("id").asText();
        this.token = "Bearer " + objectMapper.readTree(response).get("data").get("token").asText();

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
    @DisplayName("Scenario 1: Atomic Bid Updates (Contention)")
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
                    startLatch.await();
                    
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

        startLatch.countDown();
        doneLatch.await(300, TimeUnit.SECONDS);
        executorService.shutdown();

        Item finalItem = itemRepo.findById(itemId).orElseThrow();
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        boolean found = attemptedBids.stream()
                .anyMatch(bid -> bid.compareTo(finalItem.getCurrentBid()) == 0);
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Scenario 2: Lock Timeout & Recovery (JPA @Version Safety Net)")
    void testLockTimeoutAndRecovery() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        MethodInterceptor interceptor = invocation -> {
            if ("save".equals(invocation.getMethod().getName()) && callCount.incrementAndGet() == 1) {
                Thread.sleep(8000);
            }
            return invocation.proceed();
        };

        ProxyFactory factory = new ProxyFactory(itemRepo);
        factory.addAdvice(interceptor);
        ItemRepository proxiedRepo = (ItemRepository) factory.getProxy();

        ItemRepository originalRepo = (ItemRepository) ReflectionTestUtils.getField(auctionService, "itemRepo");
        ReflectionTestUtils.setField(auctionService, "itemRepo", proxiedRepo);

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            Future<MvcResult> futureA = executorService.submit(() -> {
                BidRequestDTO bidRequest = new BidRequestDTO(BigDecimal.valueOf(210.00));
                return mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                        .andReturn();
            });

            Thread.sleep(1000);

            Future<MvcResult> futureB = executorService.submit(() -> {
                Thread.sleep(6000); 
                BidRequestDTO bidRequest = new BidRequestDTO(BigDecimal.valueOf(220.00));
                return mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                        .andReturn();
            });

            MvcResult resultB = futureB.get();
            MvcResult resultA = futureA.get();

            assertThat(resultB.getResponse().getStatus()).isEqualTo(200);
            assertThat(resultA.getResponse().getStatus()).isEqualTo(409);
            
            Item finalItem = itemRepo.findById(itemId).orElseThrow();
            assertThat(finalItem.getCurrentBid().compareTo(BigDecimal.valueOf(220.00))).isEqualTo(0);
            
            executorService.shutdown();
        } finally {
            ReflectionTestUtils.setField(auctionService, "itemRepo", originalRepo);
        }
    }

    @Test
    @DisplayName("Scenario 3: Transaction Rollback & Consistency Safety")
    void testTransactionRollbackConsistency() throws Exception {
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
            BigDecimal bidAmount = BigDecimal.valueOf(500.00);
            BidRequestDTO bidRequest = new BidRequestDTO(bidAmount);

            mockMvc.perform(put(baseUrl + "/auctions/" + auctionId + "/items/" + itemId + "/bid")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bidRequest)))
                    .andExpect(status().isServiceUnavailable());

            Item finalItem = itemRepo.findById(itemId).orElseThrow();
            assertThat(finalItem.getCurrentBid()).isNull();

            String redisKey = "auction:item:bids:" + itemId;
            Double score = redisTemplate.opsForZSet().score(redisKey, userId);
            assertThat(score).isNull();

        } finally {
            ReflectionTestUtils.setField(auctionService, "itemRepo", originalRepo);
        }
    }

    @Test
    @DisplayName("Scenario 4: Lock Mutual Exclusion (Per-Item Scope)")
    void testLockMutualExclusion() throws Exception {
        User owner = userRepo.findByEmail("name2@domain.tld").orElseThrow();
        Item item1 = Data.generateItem();
        Item item2 = Data.generateItem();
        item1.setDescription("Unique Description 1");
        item2.setDescription("Unique Description 2");
        item1.setInitialPrice(BigDecimal.valueOf(100.00));
        item2.setInitialPrice(BigDecimal.valueOf(100.00));
        
        Auction auction = Data.generateAuction(Set.of(item1, item2));
        auction.setUser(owner);
        auction.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        auction.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));
        Auction savedAuction = auctionRepo.save(auction);
        
        UUID iid1 = savedAuction.getItems().stream().filter(i -> i.getDescription().equals(item1.getDescription())).findFirst().orElseThrow().getId();
        UUID iid2 = savedAuction.getItems().stream().filter(i -> i.getDescription().equals(item2.getDescription())).findFirst().orElseThrow().getId();

        MethodInterceptor interceptor = invocation -> {
            if ("save".equals(invocation.getMethod().getName())) {
                Item itemArg = (Item) invocation.getArguments()[0];
                if (itemArg.getId().equals(iid1)) {
                    Thread.sleep(4000);
                }
            }
            return invocation.proceed();
        };

        ProxyFactory factory = new ProxyFactory(itemRepo);
        factory.addAdvice(interceptor);
        ItemRepository proxiedRepo = (ItemRepository) factory.getProxy();
        ItemRepository originalRepo = (ItemRepository) ReflectionTestUtils.getField(auctionService, "itemRepo");
        ReflectionTestUtils.setField(auctionService, "itemRepo", proxiedRepo);

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            BidRequestDTO commonRequest = new BidRequestDTO(BigDecimal.valueOf(110.00));

            Future<MvcResult> futureA = executorService.submit(() -> {
                return mockMvc.perform(put(baseUrl + "/auctions/" + savedAuction.getId() + "/items/" + iid1 + "/bid")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commonRequest)))
                        .andReturn();
            });

            Thread.sleep(500);

            long startTimeB = System.currentTimeMillis();
            MvcResult resultB = mockMvc.perform(put(baseUrl + "/auctions/" + savedAuction.getId() + "/items/" + iid2 + "/bid")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commonRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
            long durationB = System.currentTimeMillis() - startTimeB;

            MvcResult resultC = mockMvc.perform(put(baseUrl + "/auctions/" + savedAuction.getId() + "/items/" + iid1 + "/bid")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new BidRequestDTO(BigDecimal.valueOf(120.00)))))
                    .andExpect(status().isServiceUnavailable())
                    .andReturn();

            MvcResult resultA = futureA.get();

            assertThat(resultA.getResponse().getStatus()).isEqualTo(200);
            assertThat(resultB.getResponse().getStatus()).isEqualTo(200);
            assertThat(durationB).isLessThan(2000);
            assertThat(resultC.getResponse().getStatus()).isEqualTo(503);

            executorService.shutdown();
        } finally {
            ReflectionTestUtils.setField(auctionService, "itemRepo", originalRepo);
        }
    }

    @Test
    @DisplayName("Scenario 5: High Contention Stress (50 Threads)")
    void testHighContentionStress() throws Exception {
        int numberOfThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 1; i <= numberOfThreads; i++) {
            BigDecimal bidAmount = BigDecimal.valueOf(200.00 + i);
            executorService.submit(() -> {
                try {
                    startLatch.await();
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
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(300, TimeUnit.SECONDS);
        executorService.shutdown();

        System.out.println("--- Stress Test Results ---");
        System.out.println("Successes: " + successCount.get());
        System.out.println("Conflicts: " + conflictCount.get());
        System.out.println("Errors: " + errorCount.get());

        assertThat(errorCount.get()).isEqualTo(0);
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(numberOfThreads);
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }
}
