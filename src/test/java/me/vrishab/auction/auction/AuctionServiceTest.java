package me.vrishab.auction.auction;

import me.vrishab.auction.auction.AuctionException.AuctionForbiddenBidingPhaseException;
import me.vrishab.auction.auction.AuctionException.AuctionItemNotFoundException;
import me.vrishab.auction.auction.AuctionException.InvalidBidAmountException;
import me.vrishab.auction.auction.AuctionException.UnauthorizedAuctionAccess;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import me.vrishab.auction.user.model.User;
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

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static me.vrishab.auction.TestData.generateAuctions;
import static me.vrishab.auction.TestData.generateUsers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    AuctionRepository auctionRepo;

    @Mock
    UserRepository userRepo;

    @Mock
    ItemRepository itemRepo;

    @InjectMocks
    AuctionService auctionService;

    List<Auction> auctions;

    User ownerUser;

    User otherUser;


    @BeforeEach
    void setUp() {

        Iterator<User> users = generateUsers().iterator();

        this.ownerUser = users.next();

        this.otherUser = users.next();

        this.auctions = generateAuctions(ownerUser, otherUser);

    }

    @AfterEach
    void tearDown() {
        this.auctions.clear();
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
        given(auctionRepo.findById(Mockito.any(UUID.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> auctionService.findById("a6c9417c-d01a-40e9-a22d-7621fd31a8c0"));

        // Then
        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c0");

    }

    @Test
    void testFindAllAuctionSuccess() {

        // Given
        Pageable pageable = Pageable.unpaged();
        given(auctionRepo.findAll(eq(pageable)))
                .willReturn(new PageImpl<>(
                                this.auctions
                        )
                );

        // When
        List<Auction> returnedAuctions = auctionService.findAll(new PageRequestParams(null, null));

        // Then
        assertThat(returnedAuctions).hasSize(9);

        verify(auctionRepo, times(1)).findAll(pageable);
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
        newItem.setInitialPrice(BigDecimal.valueOf(100.00));

        Auction newAuction = new Auction();

        newAuction.setId(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));
        newAuction.setName("Auction 1");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        newAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        newAuction.setEndTime(calendar.getTime().toInstant());
        newAuction.addAllItems(Set.of(newItem));
        newAuction.setUser(this.ownerUser);
        given(auctionRepo.save(newAuction)).willReturn(newAuction);

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(userRepo.findById(userId))
                .willReturn(Optional.of(ownerUser));

        // When
        Auction savedAuction = this.auctionService.add("9a540a1e-b599-4cec-aeb1-6396eb8fa270", newAuction);

        // Then
        assertAll(
                () -> assertThat(savedAuction.getId().toString()).isEqualTo("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                () -> assertThat(savedAuction.getItems().size()).isEqualTo(1)
        );

        assertAll(
                () -> assertThat(savedAuction.getItems()).allMatch(item -> item.getSeller().equals(ownerUser.getEmail())),
                () -> assertThat(savedAuction.getOwnerEmail()).isEqualTo(ownerUser.getEmail())
        );

        verify(auctionRepo, times(1)).save(newAuction);
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void testUpdateAuctionSuccess() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item oldItem = new Item();
        oldItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        oldItem.setName("Item");
        oldItem.setDescription("Description");
        oldItem.setLocation("MA");
        oldItem.setImageUrls(Set.of("<images>"));
        oldItem.setLegitimacyProof("Proof");
        oldItem.setInitialPrice(BigDecimal.valueOf(100.00));
        oldItem.setSeller("name0@domain.tld");

        Auction oldAuction = new Auction();

        oldAuction.setId(auctionId);
        oldAuction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        oldAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        oldAuction.setEndTime(calendar.getTime().toInstant());
        oldAuction.addAllItems(Set.of(oldItem));
        oldAuction.setUser(ownerUser);

        calendar = Calendar.getInstance();

        Item updateItem = new Item();
        updateItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        updateItem.setName("New Item");
        updateItem.setDescription("New Description");
        updateItem.setLocation("CA");
        updateItem.setImageUrls(Set.of("<images>"));
        updateItem.setExtras(null);
        updateItem.setLegitimacyProof("Proof");
        BigDecimal bidAmount = BigDecimal.valueOf(150.00);
        updateItem.setInitialPrice(bidAmount);

        Auction updateAuction = new Auction();

        updateAuction.setId(auctionId);
        updateAuction.setName("New Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        updateAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        updateAuction.setEndTime(calendar.getTime().toInstant());
        updateAuction.addAllItems(Set.of(updateItem));
        updateAuction.setUser(ownerUser);

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(userRepo.findById(userId))
                .willReturn(Optional.of(ownerUser));

        given(auctionRepo.findById(auctionId))
                .willReturn(Optional.of(oldAuction));
        given(auctionRepo.save(oldAuction)).willReturn(oldAuction);

        given(itemRepo.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1")))
                .willReturn(Optional.of(oldItem));

        // When
        Auction updatedAuction = this.auctionService.update("9a540a1e-b599-4cec-aeb1-6396eb8fa270", updateAuction, "a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        // Then
        assertAll(
                () -> assertThat(updatedAuction.getId().toString()).isEqualTo("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"),
                () -> assertThat(updateAuction.getItems().size()).isEqualTo(1),
                () -> assertThat(updatedAuction.getItems().iterator().next().getInitialPrice()).isEqualTo(bidAmount),
                () -> assertThat(updateAuction.getName()).isEqualTo("New Auction"),
                () -> assertThat(updatedAuction.getItems().iterator().next().getName()).isEqualTo("New Item")
        );
        assertAll(
                () -> assertThat(updatedAuction.getItems()).allMatch(item -> item.getSeller().equals(ownerUser.getEmail()))
        );

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);

    }

    @Test
    void testUpdateAuctionUnauthorized() {

        // Given
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item oldItem = new Item();
        oldItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        oldItem.setName("Item");
        oldItem.setDescription("Description");
        oldItem.setLocation("MA");
        oldItem.setImageUrls(Set.of("<images>"));
        oldItem.setLegitimacyProof("Proof");
        oldItem.setSeller("name0@domain.tld");
        oldItem.setInitialPrice(BigDecimal.valueOf(100.00));

        Auction oldAuction = new Auction();

        oldAuction.setId(auctionId);
        oldAuction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        oldAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        oldAuction.setEndTime(calendar.getTime().toInstant());

        oldAuction.setUser(ownerUser);
        oldAuction.addAllItems(Set.of(oldItem));

        calendar = Calendar.getInstance();

        Item updateItem = new Item();
        updateItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        updateItem.setName("New Item");
        updateItem.setDescription("New Description");
        updateItem.setLocation("CA");
        updateItem.setImageUrls(Set.of("<images>"));
        updateItem.setExtras(null);
        updateItem.setLegitimacyProof("Proof");
        updateItem.setInitialPrice(BigDecimal.valueOf(150.00));

        Auction updateAuction = new Auction();

        updateAuction.setId(auctionId);
        updateAuction.setName("New Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        updateAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        updateAuction.setEndTime(calendar.getTime().toInstant());
        updateAuction.addAllItems(Set.of(updateItem));
        updateAuction.setUser(ownerUser);


        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(userRepo.findById(userId))
                .willReturn(Optional.of(otherUser));

        given(auctionRepo.findById(auctionId))
                .willReturn(Optional.of(oldAuction));

        // When

        Throwable thrown = catchThrowable(() -> this.auctionService.update("9a540a1e-b599-4cec-aeb1-6396eb8fa270", updateAuction, "a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));

        assertThat(thrown).isInstanceOf(UnauthorizedAuctionAccess.class).hasMessage("The user not an owner of the auction");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
    }

    @Test
    void testUpdateAuctionItemNotFound() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item oldItem = new Item();

        oldItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        oldItem.setName("Item");
        oldItem.setDescription("Description");
        oldItem.setLocation("MA");
        oldItem.setImageUrls(Set.of("<images>"));
        oldItem.setLegitimacyProof("Proof");
        oldItem.setInitialPrice(BigDecimal.valueOf(100.00));
        oldItem.setSeller("name0@domain.tld");

        Auction oldAuction = new Auction();

        oldAuction.setId(auctionId);
        oldAuction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        oldAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        oldAuction.setEndTime(calendar.getTime().toInstant());

        oldAuction.setUser(ownerUser);

        calendar = Calendar.getInstance();

        Item updateItem = new Item();
        updateItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a2"));
        updateItem.setName("New Item");
        updateItem.setDescription("New Description");
        updateItem.setLocation("CA");
        updateItem.setImageUrls(Set.of("<images>"));
        updateItem.setExtras(null);
        updateItem.setInitialPrice(BigDecimal.valueOf(150.00));
        updateItem.setLegitimacyProof("Proof");

        Auction updateAuction = new Auction();

        updateAuction.setId(auctionId);
        updateAuction.setName("New Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        updateAuction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 3);
        updateAuction.setEndTime(calendar.getTime().toInstant());
        updateAuction.addAllItems(Set.of(updateItem));


        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(userRepo.findById(userId))
                .willReturn(Optional.of(ownerUser));

        given(auctionRepo.findById(auctionId))
                .willReturn(Optional.of(oldAuction));

        Item otherItem = new Item();
        otherItem.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a2"));
        otherItem.setName("Item");
        otherItem.setDescription("Description");
        otherItem.setLocation("MA");
        otherItem.setImageUrls(Set.of("<images>"));
        otherItem.setExtras(null);
        otherItem.setLegitimacyProof("Proof");
        otherItem.setSeller(otherUser.getEmail());
        given(itemRepo.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a2"))).willReturn(Optional.of(otherItem));

        // When

        Throwable thrown = catchThrowable(() -> this.auctionService.update("9a540a1e-b599-4cec-aeb1-6396eb8fa270", updateAuction, "a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));

        assertThat(thrown).isInstanceOf(AuctionItemNotFoundException.class).hasMessage("The Item with Id e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a2 is not in the auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
    }

    @Test
    void testDeleteAuctionSuccess() {

        // Given
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.setUser(ownerUser);
        auction.addAllItems(Set.of(item));


        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(ownerUser));
        doNothing().when(this.auctionRepo).deleteById(auctionId);

        // When
        auctionService.delete("9a540a1e-b599-4cec-aeb1-6396eb8fa270", "a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        // Then
        verify(userRepo, times(1)).findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270"));
        verify(auctionRepo, times(1)).findById(auctionId);
        verify(auctionRepo, times(1)).deleteById(UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));

    }

    @Test
    void testDeleteAuctionUnauthorizedAccess() {

        // Given
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.setUser(ownerUser);


        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(otherUser));

        // When


        // Then
        Throwable thrown = catchThrowable(() -> this.auctionService.delete("9a540a1e-b599-4cec-aeb1-6396eb8fa270", "a6c9417c-d01a-40e9-a22d-7621fd31a8c1"));

        assertThat(thrown).isInstanceOf(UnauthorizedAuctionAccess.class).hasMessage("The user not an owner of the auction");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);

    }

    @Test
    void testPlaceBidSuccess() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        UUID itemId = UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1");
        item.setId(itemId);
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        auction.setStartTime(calendar.getTime().toInstant().minus(1, ChronoUnit.HOURS));
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.setUser(ownerUser);
        auction.addAllItems(Set.of(item));

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(itemRepo.findById(itemId))
                .willReturn(Optional.of(item));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(otherUser));
        given(this.itemRepo.save(item)).willReturn(item);

        // When
        BigDecimal bidAmount = BigDecimal.valueOf(150.00);
        Item returnedItem = this.auctionService.bid("9a540a1e-b599-4cec-aeb1-6396eb8fa271",
                "a6c9417c-d01a-40e9-a22d-7621fd31a8c1",
                "e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1",
                bidAmount
        );

        // Then
        assertAll(
                () -> assertThat(returnedItem.getCurrentBid()).isEqualTo(bidAmount)
        );
        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
        verify(itemRepo, times(1)).findById(itemId);
        verify(itemRepo, times(1)).save(item);
    }

    @Test
    void testPlaceBidUnAuthorizedAccess() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        auction.setStartTime(calendar.getTime().toInstant().minus(1, ChronoUnit.HOURS));
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.setUser(ownerUser);
        auction.addAllItems(Set.of(item));

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa270");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(ownerUser));
        given(itemRepo.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1")))
                .willReturn(Optional.of(item));

        // When
        Throwable thrown = catchThrowable(() ->
                this.auctionService.bid("9a540a1e-b599-4cec-aeb1-6396eb8fa270",
                        "a6c9417c-d01a-40e9-a22d-7621fd31a8c1",
                        "e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1",
                        BigDecimal.valueOf(150.0)
                ));


        // Then
        assertThat(thrown)
                .isInstanceOf(UnauthorizedAuctionAccess.class)
                .hasMessage("The user is an owner of the auction");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
    }

    @Test
    void testPlaceBidNotInBidingPhase() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setStartTime(calendar.getTime().toInstant());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.addAllItems(Set.of(item));
        auction.setUser(ownerUser);

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(otherUser));
        given(itemRepo.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"))).willReturn(Optional.of(item));

        // When
        Throwable thrown = catchThrowable(() ->
                this.auctionService.bid("9a540a1e-b599-4cec-aeb1-6396eb8fa271",
                        "a6c9417c-d01a-40e9-a22d-7621fd31a8c1",
                        "e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1",
                        BigDecimal.valueOf(150.0)
                ));


        // Then
        assertThat(thrown)
                .isInstanceOf(AuctionForbiddenBidingPhaseException.class)
                .hasMessage("Auction with Id a6c9417c-d01a-40e9-a22d-7621fd31a8c1 is not in Biding Phase");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
    }

    @Test
    void testPlaceBidInvalidAmount() {
        Calendar calendar = Calendar.getInstance();

        UUID auctionId = UUID.fromString("a6c9417c-d01a-40e9-a22d-7621fd31a8c1");

        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1"));
        item.setName("Item");
        item.setDescription("Description");
        item.setLocation("MA");
        item.setImageUrls(Set.of("<images>"));
        item.setLegitimacyProof("Proof");
        item.setInitialPrice(BigDecimal.valueOf(100.00));
        item.setSeller("name0@domain.tld");

        Auction auction = new Auction();

        auction.setId(auctionId);
        auction.setName("Auction");
        calendar.setTime(new Date());
        auction.setStartTime(calendar.getTime().toInstant().minus(1, ChronoUnit.HOURS));
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        auction.setEndTime(calendar.getTime().toInstant());
        auction.addAllItems(Set.of(item));
        auction.setUser(ownerUser);

        UUID userId = UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        given(auctionRepo.findById(auctionId)).willReturn(Optional.of(auction));
        given(userRepo.findById(userId))
                .willReturn(Optional.of(otherUser));
        given(itemRepo.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1")))
                .willReturn(Optional.of(item));

        // When
        Throwable thrown = catchThrowable(() ->
                this.auctionService.bid("9a540a1e-b599-4cec-aeb1-6396eb8fa271",
                        "a6c9417c-d01a-40e9-a22d-7621fd31a8c1",
                        "e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a1",
                        BigDecimal.valueOf(50.0)
                ));


        // Then
        assertThat(thrown)
                .isInstanceOf(InvalidBidAmountException.class)
                .hasMessage("Bid amount must be higher than the current bid");

        verify(userRepo, times(1)).findById(userId);
        verify(auctionRepo, times(1)).findById(auctionId);
    }


}