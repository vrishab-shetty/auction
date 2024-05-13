package me.vrishab.auction.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository repository;

    @InjectMocks
    private ItemService service;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() {
        // Given. Input and targets. Define the behavior of Mock object
        /*
            "id": "e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3",
            "name": "Hammer",
            "description": "A tool used for driving nails or breaking objects by striking.",
            "location": "NEU, Boston, MA",
            "images": [
              "<images>"
            ],
            "legitimacyProof": "proof",
            "extras": null,
            "auctionId": "c4838b19-9c96-45e0-abd7-c77d91af22b2",
            "seller": "vr@domain.tld"
        * */
        Item item = new Item();
        item.setId(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"));
        item.setName("Hammer");
        item.setDescription("A tool used for driving nails or breaking objects by striking.");
        item.setLocation("NEU, Boston, MA");
        item.setImageUrls(Set.of("<images>"));
        item.setExtras(null);
        item.setLegitimacyProof("Proof");
        item.setAuctionId(UUID.fromString("c4838b19-9c96-45e0-abd7-c77d91af22b2"));
        item.setSeller("vr@domain.tld");

        given(repository.findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"))).willReturn(Optional.of(item));

        // When. Act on the target behavior. When steps should cove the method to be tested
        Item returnedItem = service.findById("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3");

        // Then. Compare the result form When to Given
        assertThat(returnedItem.getId()).isEqualTo(item.getId());
        assertThat(returnedItem.getName()).isEqualTo(item.getName());
        assertThat(returnedItem.getDescription()).isEqualTo(item.getDescription());
        assertThat(returnedItem.getLocation()).isEqualTo(item.getLocation());
        assertThat(returnedItem.getImageUrls()).isEqualTo(item.getImageUrls());
        assertThat(returnedItem.getExtras()).isEqualTo(item.getExtras());
        assertThat(returnedItem.getLegitimacyProof()).isEqualTo(item.getLegitimacyProof());
        assertThat(returnedItem.getAuctionId()).isEqualTo(item.getAuctionId());
        assertThat(returnedItem.getSeller()).isEqualTo(item.getSeller());

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
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("Could not find item with Id e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3");
        verify(repository, times(1)).findById(UUID.fromString("e2b2dd83-0e5d-4d73-b5cc-744f3fdc49a3"));
    }
}