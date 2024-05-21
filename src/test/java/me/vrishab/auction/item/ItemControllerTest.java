package me.vrishab.auction.item;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
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

        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a" + i));
            item.setName("Item " + i + (i % 2 == 0 ? " (special)" : ""));
            item.setDescription("Description " + i);
            item.setLocation(i % 3 == 1 ? "MA" : "CA");
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b" + i % 2));
            item.setLegitimacyProof("Proof");
            item.setSeller("vr@domain.tld");
            this.items.add(item);
        }
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
                .andExpect(jsonPath("$.data.name").value("Item 0 (special)"));
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

    @Test
    void testFindAllItemsPaginationSuccess() throws Exception {

        // Given
        int page = 0, size = 4;
        given(itemService.findAllPagination(page, size)).willReturn(new PageImpl<>(
                items.subList(page * size, (page + 1) * size)
        ));

        // When and Then
        this.mockMvc.perform(get("/api/v1/items")
                        .param("pageNum", String.valueOf(page))
                        .param("pageSize", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items"))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(size)));
    }

    @Test
    void testFindAllItemsSearchByName() throws Exception {

        // Given
        String name = "special";

        given(itemService.searchAllByName(name))
                .willReturn(items.stream().filter(item -> item.getName().contains(name)).toList());

        // When and Then
        this.mockMvc.perform(get("/api/v1/items")
                        .param("query", name)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items with name containing " + name))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(5)))
                .andExpect(jsonPath("$.data[*].name").value(Matchers.everyItem(Matchers.containsString(name))));
    }

    @Test
    void testFindAllItemsByLocation() throws Exception {

        // Given
        String location = "MA";

        given(itemService.findAllByLocation(location))
                .willReturn(items.stream().filter(item -> item.getLocation().contains(location)).toList());

        // When and Then
        this.mockMvc.perform(get("/api/v1/items")
                        .param("location", location)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items with location " + location))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[*].location").value(Matchers.everyItem(Matchers.equalTo(location))));

    }


}