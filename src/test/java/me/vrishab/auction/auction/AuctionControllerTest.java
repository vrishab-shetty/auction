package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.AuctionCreationDTO;
import me.vrishab.auction.auction.dto.AuctionUpdateDTO;
import me.vrishab.auction.auction.dto.BidRequestDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.AuctionItemUpdateDTO;
import me.vrishab.auction.item.dto.ItemCreationDTO;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.model.User;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static me.vrishab.auction.TestData.generateAuctions;
import static me.vrishab.auction.TestData.generateUsers;
import static me.vrishab.auction.auction.AuctionException.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private AuthService authService;

    private List<Auction> auctions;

    private User user;

    private User buyer;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {

        Iterator<User> users = generateUsers().iterator();
        this.user = users.next();
        this.buyer = users.next();

        this.auctions = generateAuctions(this.user, this.buyer);
    }

    @AfterEach
    void tearDown() {
        this.auctions.clear();
        this.user = null;
    }

    @Test
    void testFindAuctionByIdSuccess() throws Exception {

        // Given
        given(auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c1")).willReturn(
                this.auctions.get(1)
        );

        // Then and When
        this.mockMvc.perform(get(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find Auctions"))
                .andExpect(jsonPath("$.data.id").value("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));
    }

    @Test
    void testFindAuctionByIdNotFound() throws Exception {

        // Given
        UUID id = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");
        given(auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c1")).willThrow(new AuctionNotFoundByIdException(id));

        // Then and When
        this.mockMvc.perform(get(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1").accept(MediaType.APPLICATION_JSON))
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
        this.mockMvc.perform(get(baseUrl + "/auctions").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all Auctions"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.auctions.size())));
    }

    @Test
    void testAddAuctionSuccess() throws Exception {

        // Given
        Auction newAuction = this.auctions.get(0);
        String id = user.getId().toString();

        AuctionCreationDTO auctionCreationDTO = new AuctionCreationDTO(
                newAuction.getName(),
                newAuction.getStartTime(),
                newAuction.getEndTime(),
                newAuction.getItems().stream().map(item -> new ItemCreationDTO(
                        item.getName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getInitialPrice(),
                        item.getImageUrls(),
                        item.getLegitimacyProof(),
                        item.getExtras()
                )).toList()
        );

        given(auctionService.add(eq(id), Mockito.any(Auction.class))).willReturn(
                newAuction
        );
        given(authService.getUserInfo(Mockito.any())).willReturn(id);

        String json = this.objectMapper.writeValueAsString(auctionCreationDTO);

        this.mockMvc.perform(post(baseUrl + "/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add an Auction"))
                .andExpect(jsonPath("$.data.name").value("Auction 0"))
                .andExpect(jsonPath("$.data.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.user").value(this.user.getEmail()));
    }

    @Test
    void testUpdateAuctionSuccess() throws Exception {
        Auction update = this.auctions.get(0);
        String id = user.getId().toString();

        AuctionUpdateDTO auctionCreationDTO = new AuctionUpdateDTO(
                update.getName(),
                update.getStartTime(),
                update.getEndTime(),
                update.getItems().stream().map(item -> new AuctionItemUpdateDTO(
                        item.getId().toString(),
                        item.getName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getInitialPrice(),
                        item.getImageUrls(),
                        item.getLegitimacyProof(),
                        item.getExtras()
                )).toList()
        );

        given(auctionService.update(eq(id), Mockito.any(Auction.class), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"))).willReturn(
                update
        );
        given(authService.getUserInfo(Mockito.any())).willReturn(id);

        String json = this.objectMapper.writeValueAsString(auctionCreationDTO);

        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update an Auction"))
                .andExpect(jsonPath("$.data.name").value("Auction 0"))
                .andExpect(jsonPath("$.data.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.user").value(this.user.getEmail()));
    }

    @Test
    void testUpdateAuctionUnAuthorized() throws Exception {
        Auction update = this.auctions.get(0);
        String id = user.getId().toString();

        AuctionCreationDTO auctionCreationDTO = new AuctionCreationDTO(
                update.getName(),
                update.getStartTime(),
                update.getEndTime(),
                update.getItems().stream().map(item -> new ItemCreationDTO(
                        item.getName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getInitialPrice(),
                        item.getImageUrls(),
                        item.getLegitimacyProof(),
                        item.getExtras()
                )).toList()
        );

        given(auctionService.update(eq(id), Mockito.any(Auction.class), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"))).willThrow(
                new UnauthorizedAuctionAccess(false)
        );
        given(authService.getUserInfo(Mockito.any())).willReturn(id);

        String json = this.objectMapper.writeValueAsString(auctionCreationDTO);

        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("The user not an owner of the auction"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateAuctionForbiddenUpdate() throws Exception {
        Auction update = this.auctions.get(0);
        String id = user.getId().toString();

        AuctionCreationDTO auctionCreationDTO = new AuctionCreationDTO(
                update.getName(),
                update.getStartTime(),
                update.getEndTime(),
                update.getItems().stream().map(item -> new ItemCreationDTO(
                        item.getName(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getInitialPrice(),
                        item.getImageUrls(),
                        item.getLegitimacyProof(),
                        item.getExtras()
                )).toList()
        );

        given(auctionService.update(eq(id), Mockito.any(Auction.class), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"))).willThrow(
                new AuctionForbiddenUpdateException(update.getId())
        );
        given(authService.getUserInfo(Mockito.any())).willReturn(id);

        String json = this.objectMapper.writeValueAsString(auctionCreationDTO);

        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Auction with id a6c9417c-d01a-40e9-a22d-7621fd31a8c0 has already began or ended and cannot be modified"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteAuctionSuccess() throws Exception {
        doNothing().when(this.auctionService).delete(eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"));
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        this.mockMvc.perform(delete(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete an Auction"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteAuctionUnauthorized() throws Exception {
        doThrow(new UnauthorizedAuctionAccess(false)).when(this.auctionService).delete(eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"));
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        this.mockMvc.perform(delete(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("The user not an owner of the auction"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteAuctionForbiddenUpdate() throws Exception {

        doThrow(new AuctionForbiddenUpdateException(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))).when(this.auctionService).delete(eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"), eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"));
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        this.mockMvc.perform(delete(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided Auction with id 9a540a1e-b599-4cec-aeb1-6396eb8fa271 has already began or ended and cannot be modified"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testPlaceBidSuccess() throws Exception {

        Auction auction = this.auctions.get(1);

        BigDecimal bidAmount = BigDecimal.valueOf(150.0);

        Item item = auction.getItems().iterator().next();
        item.setCurrentBid(bidAmount);

        String itemId = item.getId().toString();
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        given(this.auctionService.bid(
                eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"),
                eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                eq(item.getId().toString()),
                eq(bidAmount))
        ).willReturn(item);

        BidRequestDTO bidRequestDTO = new BidRequestDTO(
                bidAmount
        );

        String json = this.objectMapper.writeValueAsString(bidRequestDTO);
        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1/items/" + itemId + "/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Place a Bid"))
                .andExpect(jsonPath("$.data.currentBid").value("150.0"))
                .andExpect(jsonPath("$.data.buyer").value(this.buyer.getEmail()));


    }

    @Test
    void testPlaceBidNotInBidingPhase() throws Exception {

        Auction auction = this.auctions.get(1);
        Item item = auction.getItems().iterator().next();
        String itemId = item.getId().toString();
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        given(this.auctionService.bid(
                eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"),
                eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                eq(itemId),
                eq(BigDecimal.valueOf(150.0)))
        ).willThrow(
                new AuctionForbiddenBidingPhaseException(auction.getId())
        );

        BidRequestDTO bidRequestDTO = new BidRequestDTO(
                BigDecimal.valueOf(150.0)
        );

        String json = this.objectMapper.writeValueAsString(bidRequestDTO);
        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1/items/" + itemId + "/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c1 is not in Biding Phase"))
                .andExpect(jsonPath("$.data").isEmpty());


    }

    @Test
    void testPlaceBidInvalidBidAmount() throws Exception {

        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        BigDecimal bidAmount = BigDecimal.valueOf(50.0);
        given(this.auctionService.bid(
                eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"),
                eq("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                eq("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"),
                eq(bidAmount))
        ).willThrow(
                new InvalidBidAmountException()
        );

        BidRequestDTO bidRequestDTO = new BidRequestDTO(bidAmount);

        String json = this.objectMapper.writeValueAsString(bidRequestDTO);
        this.mockMvc.perform(put(baseUrl + "/auctions/a6c9417c-d01a-40e9-a22d-7621fd31a8c1/items/e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Bid amount must be higher than the current bid"))
                .andExpect(jsonPath("$.data").isEmpty());


    }
}