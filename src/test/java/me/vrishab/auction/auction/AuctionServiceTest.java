package me.vrishab.auction.auction;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.system.PageRequestParams;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    AuctionRepository auctionRepo;

    @InjectMocks
    AuctionService auctionService;

    List<Auction> auctions;

    @BeforeEach
    void setUp() {
        auctions = new ArrayList<>();

        Item item = new Item();
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setExtras(null);
        item.setLegitimacyProof("Proof");
        item.setSeller("vr@domain.tld");

        for (int i = 0; i < 10; i++) {
            Auction auction = new Auction();

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, i % 2);
            auction.setStartTime(calendar.getTime().toInstant());
            calendar.add(Calendar.HOUR_OF_DAY, i % 3);
            auction.setEndTime(calendar.getTime().toInstant());
            auction.setInitialPrice(100.00);
            auction.setBuyer("name1@domain.tld");
            auction.setItems(Set.of(item));
            auctions.add(auction);
        }
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testFindAuctionByIdSuccess() {

        // Given
        UUID id = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c0");
        given(auctionRepo.findById(id)).willReturn(
                Optional.ofNullable(this.auctions.get(0))
        );

        // When
        Auction returnedAuction = auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c0");

        // Then
        assertThat(
                returnedAuction.getId()
        ).isEqualTo(id);
        verify(auctionRepo, times(1)).findById(id);
    }

    @Test
    void testFindAuctionByNotFound() {

        // Given
        UUID id = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c0");
        given(auctionRepo.findById(Mockito.any(UUID.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> {
            auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c0");
        });

        // Then
        assertThat(thrown)
                .isInstanceOf(AuctionNotFoundException.class)
                .hasMessage("Could not find item with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c0");

    }

    @Test
    void testFindAllAuctionSuccess() {

        // Given
        given(auctionRepo.findAll(eq(Pageable.unpaged())))
                .willReturn(new PageImpl<>(
                                this.auctions
                        )
                );

        // When
        List<Auction> returnedAuctions = auctionService.findAll(new PageRequestParams(null, null));

        // Then
        assertThat(returnedAuctions).hasSize(10);
    }

    @Test
    void testFindAllAuctionPaginationSuccess() {

        // Given
        int page = 2, size = 3;
        Pageable pageable = PageRequest.of(page, size);
        given(auctionRepo.findAll(eq(pageable)))
                .willReturn(new PageImpl<>(
                        auctions.subList((page - 1) * size, Math.min(page * size, auctions.size() - 1))
                ));

        // When
        List<Auction> returnedAuction = auctionService.findAll(new PageRequestParams(page, size));

        // Then

        assertAll(
                () -> assertThat(returnedAuction.size()).isEqualTo(3),
                () -> assertThat(returnedAuction.get(0).getId()).isEqualTo(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c3"))
        );
        verify(auctionRepo, times(1)).findAll(eq(pageable));
    }
}