package me.vrishab.auction.auction;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.security.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private AuthService authService;

    private List<Auction> auctions;

    @BeforeEach
    void setUp() {
        auctions = new ArrayList<>();

        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setExtras(null);
        item.setLegitimacyProof("Proof");
        item.setSeller("vr@domain.tld");

        for (int i = 0; i < 10; i++) {
            Auction auction = new Auction();

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, i % 2);
            auction.setStartTime(calendar.getTime().toInstant());
            calendar.add(Calendar.HOUR_OF_DAY, i % 3);
            auction.setEndTime(calendar.getTime().toInstant());
            auction.setInitialPrice(100.00);
            auction.setBuyer("name1@domain.tld");
            auction.setItems(Set.of(item));
            auctions.add(auction);
        }
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void findAuctionByIdSuccess() throws Exception {

        // Given
        given(auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c1")).willReturn(
                this.auctions.get(1)
        );

        // Then and When
        this.mockMvc.perform(get("/api/v1/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find Auctions"))
                .andExpect(jsonPath("$.data.id").value("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));
    }

    @Test
    void findAuctionByIdNotFound() throws Exception {

        // Given
        UUID id = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");
        given(auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c1")).willThrow(new AuctionNotFoundException(id));

        // Then and When
        this.mockMvc.perform(get("/api/v1/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value("false"))
                .andExpect(jsonPath("$.message").value("Could not find auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}