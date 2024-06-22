package me.vrishab.auction.item;

import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.TestData;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;

    List<Item> items;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {

        this.items = TestData.generateItems();
    }

    @AfterEach
    void tearDown() {

        this.items.clear();

    }

    @Test
    void testFindItemByIdSuccess() throws Exception {
        // Given
        String testItemId = this.items.get(0).getId().toString();
        given(this.itemService.findById(testItemId)).willReturn(this.items.get(0));

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items/" + testItemId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find one Success"))
                .andExpect(jsonPath("$.data.id").value(testItemId))
                .andExpect(jsonPath("$.data.name").value("Item 0 (special)"));
    }

    @Test
    void testFindItemByIdNotFound() throws Exception {

        // Given
        String testItemId = this.items.get(0).getId().toString();
        given(this.itemService.findById(testItemId)).willThrow(new ItemNotFoundByIdException(UUID.fromString(testItemId)));

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items/" + testItemId).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could not find item with Id " + testItemId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllItems() throws Exception {

        // Given
        given(itemService.findAll(null, null, new PageRequestParams(null, null)))
                .willReturn(this.items);

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items"))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(items.size())));
    }

    @Test
    void testFindAllItemsPaginationSuccess() throws Exception {

        // Given
        int page = 1, size = 4;
        given(itemService.findAll(null, null, new PageRequestParams(page, size)))
                .willReturn(
                        items.subList(page * size, (page + 1) * size)
                );

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items")
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
        List<Item> filterItems = items.stream().filter(item -> item.getName().contains(name)).toList();
        given(itemService.findAll(name, null, new PageRequestParams(null, null)))
                .willReturn(
                        filterItems
                );

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items")
                        .param("query", name)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items"))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(5)))
                .andExpect(jsonPath("$.data[*].name").value(Matchers.everyItem(Matchers.containsString(name))));
    }

    @Test
    void testFindAllItemsByLocation() throws Exception {

        // Given
        String location = "MA";

        List<Item> filterItems = items.stream().filter(item -> item.getLocation().contains(location)).toList();
        given(itemService.findAll(null, location, new PageRequestParams(null, null)))
                .willReturn(
                        filterItems
                );

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items")
                        .param("location", location)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all items"))
                .andExpect(jsonPath("$.data").value(Matchers.hasSize(3)))
                .andExpect(jsonPath("$.data[*].location").value(Matchers.everyItem(Matchers.equalTo(location))));

    }

    @Test
    void testFindAllItemPaginationFailure() throws Exception {
        given(itemService.findAll(null, null, new PageRequestParams(null, null)))
                .willReturn(this.items);

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/items")
                        .param("pageNum", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details"))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

}