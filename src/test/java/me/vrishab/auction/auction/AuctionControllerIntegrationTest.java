package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.AuctionCreationDTO;
import me.vrishab.auction.auction.dto.BidRequestDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemService;
import me.vrishab.auction.item.dto.ItemCreationDTO;
import me.vrishab.auction.utils.Data;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration test for Authorized Auction user API endpoints")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("integration")
public class AuctionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String token;
    @Autowired
    private ItemService itemService;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultAction = this.mockMvc.perform(post(baseUrl + "/users/login")
                .with(httpBasic("name1@domain.tld", "password")));

        MvcResult mvcResult = resultAction.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @DisplayName("Check addAuction operation")
    @Test
    void testAddAuctionSuccess() throws Exception {

        Item item = Data.generateItem();
        Auction auction = Data.generateAuction(Set.of(item));

        auction.setStartTime(Instant.now().plus(1, ChronoUnit.MINUTES));
        auction.setEndTime(Instant.now().plus(2, ChronoUnit.MINUTES));

        ItemCreationDTO itemDTO = new ItemCreationDTO(
                item.getName(),
                item.getDescription(),
                item.getLocation(),
                item.getInitialPrice(),
                item.getImageUrls(),
                item.getLegitimacyProof(),
                item.getExtras()
        );

        AuctionCreationDTO auctionDTO = new AuctionCreationDTO(
                auction.getName(),
                auction.getStartTime(),
                auction.getEndTime(),
                List.of(itemDTO)
        );

        String json = this.objectMapper.writeValueAsString(auctionDTO);

        this.mockMvc.perform(post(this.baseUrl + "/auctions")
                        .header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add an Auction"))
                .andExpect(jsonPath("$.data.name").value(auction.getName()))
                .andExpect(jsonPath("$.data.items", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.user").value("name1@domain.tld"));
    }



    @DisplayName("Check placeBid operation")
    @Test
    void testPlaceBidSuccess() throws Exception {

        ResultActions resultAction = this.mockMvc.perform(get(baseUrl + "/auctions"));
        MvcResult mvcResult = resultAction.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        JSONArray auctions = json.getJSONArray("data");

        String bidingAuctionId = null;
        String itemId = null;
        for(int i = 0; i<auctions.length(); i++) {
            JSONObject auction = auctions.getJSONObject(i);
            if(!auction.getString("user").equals("name1@domain.tld")) {
                bidingAuctionId = auction.getString("id");
                itemId = auction.getJSONArray("items").getJSONObject(0).getString("id");
                break;
            }
        }

        if(bidingAuctionId == null || itemId == null) throw new RuntimeException("Could not find other auction");

        BidRequestDTO bidRequestDTO = new BidRequestDTO(
                BigDecimal.valueOf(150.0)
        );


        String jsonContent = this.objectMapper.writeValueAsString(bidRequestDTO);
        this.mockMvc.perform(put(this.baseUrl + "/auctions/"+bidingAuctionId+"/items/"+itemId+"/bid")
                        .header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Place a Bid"))
                .andExpect(jsonPath("$.data.currentBid").value("150.0"))
                .andExpect(jsonPath("$.data.buyer").value("name1@domain.tld"));
    }
}
