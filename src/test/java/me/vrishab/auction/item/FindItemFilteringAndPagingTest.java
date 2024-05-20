package me.vrishab.auction.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FindItemFilteringAndPagingTest extends SpringDataJpaApplicationTests {

    @Autowired
    private ItemRepository itemRepo;

    @Test
    void testFilterItemByName() {

        String query = "special";
        Page<Item> itemPage = itemRepo.findAllByNameLikeIgnoreCase("%" + query + "%", PageRequest.of(1, 3));
        Page<Item> itemPageSort = itemRepo.findAllByNameLikeIgnoreCase("%" + query + "%", PageRequest.of(1, 3, Sort.by("name")));

        assertAll(
                () -> assertThat(itemPage.getSize()).isEqualTo(3)
        );
        assertAll(
                () -> assertThat(itemPageSort.getSize()).isEqualTo(3),
                () -> assertThat(itemPageSort.getContent()).allMatch(item -> item.getName().contains(query))
        );

    }


    @Test
    void testFilterItemByLocation() {

        Page<Item> itemPage = itemRepo.findAllByLocation("Location 1", PageRequest.of(0, 3));

        List<Item> items = itemPage.getContent();
        assertAll(
                () -> assertThat(items.size()).isEqualTo(1),
                () -> assertThat(items.get(0).getLocation()).isEqualTo("Location 1")
        );
    }
}
