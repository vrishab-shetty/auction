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
}
