package me.vrishab.auction.auction;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.UserRepository;
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

    @Mock
    UserRepository userRepo;

    @InjectMocks
    AuctionService auctionService;

    List<Auction> auctions;

    User user;

    @BeforeEach
    void setUp() {
        auctions = new ArrayList<>();

        user = new User();
        user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270"));
        user.setName("Name");
        user.setPassword("password");
        user.setDescription("Description");
        user.setEnabled(true);
        user.setEmail("name@domain.tld");
        user.setContact("1234567890");


        for (int i = 0; i < 10; i++) {

            Item item = new Item();
            item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a" + i));
            item.setName("Item");
            item.setDescription("Description");
            item.setLocation("MA");
            item.setImageUrls(Set.of("<images>"));
            item.setExtras(null);
            item.setLegitimacyProof("Proof");
            item.setAuctionId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));

            Auction auction = new Auction();

            auction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c" + i));
            auction.setName("Auction " + i);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 2);
            auction.setStartTime(calendar.getTime().toInstant());
            calendar.add(Calendar.HOUR_OF_DAY, 1 + i % 3);
            auction.setEndTime(calendar.getTime().toInstant());
            auction.setInitialPrice(100.00);
            auction.setCurrentBid(100.00);
            auction.setBuyer("name1@domain.tld");

            auction.setItems(Set.of(item));
            auctions.add(auction);

            user.addAuction(auction);
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
                .hasMessage("Could not find auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c0");

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
        Pageable pageable = PageRequest.of(page - 1, size);
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

    @Test
    void testAddAuctionSuccess() {

        // Given
        Item newItem = new Item();
        newItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        newItem.setName("Item");
        newItem.setDescription("Description");
        newItem.setLocation("MA");
        newItem.setImageUrls(Set.of("<images>"));
        newItem.setExtras(null);
        newItem.setLegitimacyProof("Proof");

        Auction newAuction = new Auction();

        newAuction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));
        newAuction.setName("Auction 1");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        newAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        newAuction.setEndTime(calendar.getTime().toInstant());
        newAuction.setInitialPrice(100.00);
        newAuction.setBuyer("name1@domain.tld");

        given(auctionRepo.save(newAuction)).willReturn(newAuction);

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(userRepo.findById(userId))
                .willReturn(Optional.of(user));

        // When
        Auction savedAuction = this.auctionService.add("9a540a1e-b599-4cec-aeb1-6396eb8fa270", newAuction);

        // Then
        assertAll(
                () -> assertThat(savedAuction.getId().toString()).isEqualTo("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                () -> assertThat(savedAuction.getItems()).allMatch(item -> item.getSeller().equals(user.getEmail())),
                () -> assertThat(savedAuction.getCurrentBid()).isNull(),
                () -> assertThat(savedAuction.getItems()).allMatch(item -> item.getAuctionId().equals(savedAuction.getId())),
                () -> assertThat(savedAuction.getUser().getId()).isEqualTo(userId)
        );
    }
}