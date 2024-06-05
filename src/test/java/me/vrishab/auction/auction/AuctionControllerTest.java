package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.AuctionEditableDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.ItemEditableDTO;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.User;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private AuthService authService;

    private List<Auction> auctions;

    private User user;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        auctions = new ArrayList<>();

        user = new User();
        user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270"));
        user.setName("Name");
        user.setPassword("password");
        user.setDescription("Description");
        user.setEnabled(true);
        user.setEmail("name@domain.tld");
        user.setContact("1234567890");


        for (int i = 0; i < 10; i++) {

            Item item = new Item();
            item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a" + i));
            item.setName("Item");
            item.setDescription("Description");
            item.setLocation("MA");
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setLegitimacyProof("Proof");
            item.setAuctionId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));

            Auction auction = new Auction();

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 2);
            auction.setStartTime(calendar.getTime().toInstant());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 3);
            auction.setEndTime(calendar.getTime().toInstant());
            auction.setInitialPrice(100.00);
            auction.setCurrentBid(100.00);
            auction.setBuyer("name1@domain.tld");

            auction.setItems(Set.of(item));
            auctions.add(auction);

            user.addAuction(auction);
        }


    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testFindAuctionByIdSuccess() throws Exception {

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
    void testFindAuctionByIdNotFound() throws Exception {

        // Given
        UUID id = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");
        given(auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c1")).willThrow(new AuctionNotFoundException(id));

        // Then and When
        this.mockMvc.perform(get("/api/v1/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value("false"))
                .andExpect(jsonPath("$.message").value("Could not find auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c1"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllAuctionsSuccess() throws Exception {

        // Given
        given(auctionService.findAll(new PageRequestParams(null, null)))
                .willReturn(this.auctions);

        // Then and When
        this.mockMvc.perform(get("/api/v1/auctions").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all Auctions"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.auctions.size())));
    }

    @Test
    void testAddAuctionSuccess() throws Exception {

        // Given
        Auction newAuction = this.auctions.get(0);
        String id = user.getId().toString();

        AuctionEditableDTO auctionEditableDTO = new AuctionEditableDTO(
                newAuction.getName(),
                newAuction.getStartTime(),
                newAuction.getEndTime(),
                newAuction.getInitialPrice(),
                newAuction.getItems().stream().map(item -> new ItemEditableDTO(
                        item.getName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getImageUrls(),
                        item.getLegitimacyProof(),
                        item.getExtras()
                )).toList()
        );

        given(auctionService.add(eq(id), Mockito.any(Auction.class))).willReturn(
                newAuction
        );
        given(authService.getUserInfo(Mockito.any())).willReturn(id);

        String json = this.objectMapper.writeValueAsString(auctionEditableDTO);

        this.mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add an Auction"))
                .andExpect(jsonPath("$.data.name").value("Auction 0"))
                .andExpect(jsonPath("$.data.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.user").value("name@domain.tld"));
    }
}