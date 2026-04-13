package me.vrishab.auction.item;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static me.vrishab.auction.item.ItemSpecification.ItemFilterParams;
import static me.vrishab.auction.item.ItemSpecification.filterSpecification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FindItemFilteringAndPagingTest extends SpringDataJpaApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(FindItemFilteringAndPagingTest.class);
    @Autowired
    private ItemRepository itemRepo;

    @Test
    void testFilterItemByName() {

        String query = "special";
        ItemFilterParams filter = new ItemFilterParams(query, null);
        Page<Item> itemPage = itemRepo.findAll(filterSpecification(filter), PageRequest.of(1, 3));
        Page<Item> itemPageSort = itemRepo.findAll(filterSpecification(filter), PageRequest.of(1, 3, Sort.by("name")));


        log.debug("list: ", itemPage.getContent().toArray());
        assertAll(
                () -> assertThat(itemPage.getSize()).isEqualTo(3),
                () -> assertThat(itemPage.getTotalElements()).isEqualTo(5),
                () -> assertThat(itemPage.get()).allMatch(item -> item.getName().contains(query))
        );
        assertAll(
                () -> assertThat(itemPageSort.getSize()).isEqualTo(3),
                () -> assertThat(itemPageSort.getTotalElements()).isEqualTo(5),
                () -> assertThat(itemPageSort.getContent()).allMatch(item -> item.getName().contains(query))
        );

    }


    @Test
    void testFilterItemByLocation() {

        String location = "MA";
        ItemFilterParams filter = new ItemFilterParams(null, location);

        Page<Item> itemPage = itemRepo.findAll(filterSpecification(filter), PageRequest.of(0, 3));

        assertAll(
                () -> assertThat(itemPage.getSize()).isEqualTo(3)
        );
        assertAll(
                () -> assertThat(itemPage.getTotalElements()).isEqualTo(3),
                () -> assertThat(itemPage.get()).allMatch(item -> item.getLocation().equals(location))
        );
    }

    @Test
    void testFilterItemByNameAndLocation() {
        String query = "special";
        String location = "CA";

        ItemFilterParams filter = new ItemFilterParams(query, location);

        Page<Item> itemPage = itemRepo.findAll(filterSpecification(filter), PageRequest.of(0, 3));

        assertAll(
                () -> assertThat(itemPage.getSize()).isEqualTo(3)
        );
        assertAll(
                () -> assertThat(itemPage.getTotalElements()).isEqualTo(4),
                () -> assertThat(itemPage.get()).allMatch(item -> item.getLocation().equals(location)),
                () -> assertThat(itemPage.get()).allMatch(item -> item.getName().contains(query))
        );
    }

    @Test
    void testFilterItemImprovedSearch() {
        // 1. Case-insensitive name search
        String queryUpper = "SPECIAL";
        ItemFilterParams filter1 = new ItemFilterParams(queryUpper, null);
        Page<Item> page1 = itemRepo.findAll(filterSpecification(filter1), PageRequest.of(0, 10));
        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getContent()).allMatch(item -> item.getName().toLowerCase().contains("special"));

        // 2. Search in description
        String queryDesc = "Description 1";
        ItemFilterParams filter2 = new ItemFilterParams(queryDesc, null);
        Page<Item> page2 = itemRepo.findAll(filterSpecification(filter2), PageRequest.of(0, 10));
        // There is "Description 1" for item 1
        assertThat(page2.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page2.getContent()).anyMatch(item -> item.getDescription().contains(queryDesc));

        // 3. Case-insensitive location search
        String locationLower = "ma";
        ItemFilterParams filter3 = new ItemFilterParams(null, locationLower);
        Page<Item> page3 = itemRepo.findAll(filterSpecification(filter3), PageRequest.of(0, 10));
        assertThat(page3.getTotalElements()).isEqualTo(3);
        assertThat(page3.getContent()).allMatch(item -> item.getLocation().equalsIgnoreCase("MA"));
    }
}
