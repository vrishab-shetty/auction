package me.vrishab.auction.item;

import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemService service;

    private ArrayList<Item> items;

    @BeforeEach
    void setUp() {
        this.items = new ArrayList<>();

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
    void testFindByIdSuccess() {
        // Given. Input and targets. Define the behavior of Mock object
        Item item = items.get(3);

        given(repository.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"))).willReturn(Optional.of(item));

        // When. Act on the target behavior. When steps should cove the method to be tested
        Item returnedItem = service.findById("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3");

        // Then. Compare the result form When to Given
        assertAll(
                () -> assertThat(returnedItem.getId()).isEqualTo(item.getId()),
                () -> assertThat(returnedItem.getName()).isEqualTo(item.getName()),
                () -> assertThat(returnedItem.getDescription()).isEqualTo(item.getDescription()),
                () -> assertThat(returnedItem.getLocation()).isEqualTo(item.getLocation()),
                () -> assertThat(returnedItem.getImageUrls()).isEqualTo(item.getImageUrls()),
                () -> assertThat(returnedItem.getExtras()).isEqualTo(item.getExtras()),
                () -> assertThat(returnedItem.getLegitimacyProof()).isEqualTo(item.getLegitimacyProof()),
                () -> assertThat(returnedItem.getAuctionId()).isEqualTo(item.getAuctionId()),
                () -> assertThat(returnedItem.getSeller()).isEqualTo(item.getSeller())
        );


        verify(repository, times(1)).findById(item.getId());
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        given(repository.findById(Mockito.any(UUID.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> {
            service.findById("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3");
        });

        // Then
        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find item with Id e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3");
        verify(repository, times(1)).findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"));
    }

    @Test
    void testFindAllSuccess() {

        // Given
        given(repository.findAll(Mockito.any(Specification.class), eq(Pageable.unpaged())))
                .willReturn(new PageImpl<>(
                        items
                ));

        // When
        List<Item> returnedItems = service.findAll(null, null, new PageRequestParams(null, null));

        // Then
        assertThat(returnedItems.size()).isEqualTo(this.items.size());
        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(Pageable.unpaged()));
    }

    @Test
    void testFindAllPaginationSuccess() {

        // Given
        int page = 2, size = 3;
        Pageable pageable = PageRequest.of(page - 1, size);
        given(repository.findAll(Mockito.any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(
                        items.subList((page - 1) * size, Math.min(page * size, items.size() - 1))
                ));

        // When
        List<Item> returnedItemPage = service.findAll(null, null, new PageRequestParams(page, size));

        // Then
        assertAll(
                () -> assertThat(returnedItemPage.size()).isEqualTo(3),
                () -> assertThat(returnedItemPage.get(0).getId()).isEqualTo(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"))
        );
        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(pageable));
    }


    @Test
    void testSearchAllByNameSuccess() {

        // Given
        String name = "special";
        given(repository.findAll(Mockito.any(Specification.class), eq(Pageable.unpaged()))).willReturn(new PageImpl<>(
                items.stream().filter(item -> item.getName().contains(name)).toList()
        ));

        // When
        List<Item> returnedItems = service.findAll(name, null, new PageRequestParams(null, null));

        // Then
        assertThat(returnedItems).allMatch(item -> item.getName().contains(name));
        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(Pageable.unpaged()));

    }

    @Test
    void testSearchAllByNamePaginationSuccess() {

        // Given
        String name = "special";
        int page = 1, size = 3;
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Item> filterItems = items
                .stream().filter(item -> item.getName().contains(name))
                .toList();

        given(repository.findAll(Mockito.any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(
                        filterItems.subList((page - 1) * size, Math.min(page * size, filterItems.size())),
                        pageable, filterItems.size()
                ));

        // When
        List<Item> returnedItems = service.findAll(name, null, new PageRequestParams(page, size));

        // Then
        assertAll(
                () -> assertThat(returnedItems.size()).isEqualTo(3),
                () -> assertThat(returnedItems).allSatisfy(item -> assertThat(item.getName()).contains(name))
        );

        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(pageable));
    }

    @Test
    void testFindAllByLocationSuccess() {

        // Given
        String location = "MA";
        Pageable pageable = Pageable.unpaged();
        List<Item> filterItems = items
                .stream().filter(item -> item.getLocation().contains(location))
                .toList();
        given(repository.findAll(Mockito.any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(filterItems, pageable, filterItems.size()));

        // When
        List<Item> returnedItems = service.findAll(null, location, new PageRequestParams(null, null));

        // Then
        assertAll(
                () -> assertThat(returnedItems.size()).isEqualTo(filterItems.size()),
                () -> assertThat(returnedItems).allMatch(item -> item.getLocation().equals(location))
        );


        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(pageable));
    }

    @Test
    void testFindAllByLocationPaginationSuccess() {
        // Given
        String location = "CA";
        int page = 1, size = 3;
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Item> filterItems = items
                .stream().filter(item -> item.getLocation().contains(location))
                .toList();
        given(repository.findAll(Mockito.any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(filterItems.subList((page - 1) * size, Math.min(page * size, filterItems.size())),
                        pageable, filterItems.size()));

        // When
        List<Item> returnedItems = service.findAll(null, location, new PageRequestParams(page, size));

        // Then
        assertAll(
                () -> assertThat(returnedItems.size()).isEqualTo(3),
                () -> assertThat(returnedItems.size()).isEqualTo(Math.min(size, filterItems.size())),
                () -> assertThat(returnedItems).allMatch(item -> item.getLocation().equals(location))
        );


        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(pageable));
    }

    @Test
    void testFindAllByLocationAndNamePaginationSuccess() {
        // Given
        String location = "CA";
        String query = "special";
        int page = 1, size = 3;
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Item> filterItems = items
                .stream().filter(item -> item.getLocation().contains(location) && item.getName().contains(query))
                .toList();
        given(repository.findAll(Mockito.any(Specification.class), eq(pageable)))
                .willReturn(new PageImpl<>(filterItems.subList((page - 1) * size, Math.min(page * size, filterItems.size())),
                        pageable, filterItems.size()));

        // When
        List<Item> returnedItems = service.findAll(query, location, new PageRequestParams(page, size));

        // Then
        assertAll(
                () -> assertThat(returnedItems.size()).isEqualTo(3),
                () -> assertThat(returnedItems.size()).isEqualTo(Math.min(size, filterItems.size())),
                () -> assertThat(returnedItems).allMatch(item -> item.getLocation().equals(location)),
                () -> assertThat(returnedItems).allMatch(item -> item.getName().contains(query))
        );


        verify(repository, times(1)).findAll(Mockito.any(Specification.class), eq(pageable));
    }

}