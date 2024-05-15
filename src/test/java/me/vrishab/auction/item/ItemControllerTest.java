package me.vrishab.auction.item;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    List<Item> items;

    @BeforeEach
    void setUp() {

        items = new ArrayList<>();

        Item item1 = new Item();
        item1.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a4"));
        item1.setName("Nail");
        item1.setDescription("A tool used for nailing two wooden piece");
        item1.setLocation("NEU, Boston, MA");
        item1.setImageUrls(Set.of("<images>"));
        item1.setExtras(null);
        item1.setLegitimacyProof("Proof");
        item1.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b2"));
        item1.setSeller("vr@domain.tld");
        this.items.add(item1);

        Item item2 = new Item();
        item2.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"));
        item2.setName("Hammer");
        item2.setDescription("A tool used for driving nails or breaking objects by striking.");
        item2.setLocation("NEU, Boston, MA");
        item2.setImageUrls(Set.of("<images>"));
        item2.setExtras(null);
        item2.setLegitimacyProof("Proof");
        item2.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b2"));
        item2.setSeller("vr@domain.tld");
        this.items.add(item2);

        Item item3 = new Item();
        item3.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a2"));
        item3.setName("Computer");
        item3.setDescription("Electronic device");
        item3.setLocation("Amazon, San Fans, CA");
        item3.setImageUrls(Set.of("<images>"));
        item3.setExtras(null);
        item3.setLegitimacyProof("Proof");
        item3.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b1"));
        item3.setSeller("vr@domain.tld");
        this.items.add(item3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindItemByIdSuccess() throws Exception {
        // Given
        String testItemId = this.items.get(0).getId().toString();
        given(this.itemService.findById(testItemId)).willReturn(this.items.get(0));

        // When and Then
        this.mockMvc.perform(get("/api/v1/items/" + testItemId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find one Success"))
                .andExpect(jsonPath("$.data.id").value(testItemId))
                .andExpect(jsonPath("$.data.name").value("Nail"));
    }

    @Test
    void testFindItemByIdNotFound() throws Exception {

        // Given
        String testItemId = this.items.get(0).getId().toString();
        given(this.itemService.findById(testItemId)).willThrow(new ItemNotFoundException(testItemId));

        // When and Then
        this.mockMvc.perform(get("/api/v1/items/" + testItemId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could not find item with Id " + testItemId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllItems() throws Exception {

        // Given
        given(itemService.findAll()).willReturn(items);

        // When and Then
        this.mockMvc.perform(get("/api/v1/items").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items"))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(items.size())));
    }
}