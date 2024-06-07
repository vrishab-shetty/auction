package me.vrishab.auction.auction;

import jakarta.transaction.Transactional;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class AuctionService {

    private final AuctionRepository auctionRepo;

    private final UserRepository userRepo;

    private final ItemRepository itemRepo;

    public AuctionService(AuctionRepository auctionRepo, UserRepository userRepo, ItemRepository itemRepo) {
        this.auctionRepo = auctionRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    public Auction findById(String id) {
        return this.auctionRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ObjectNotFoundException("auction", UUID.fromString(id)));
    }

    public List<Auction> findAll(PageRequestParams pageSettings) {
        Pageable pageable = Pageable.unpaged();
        if (pageSettings != null && pageSettings.getPageNum() != null && pageSettings.getPageSize() != null)
            pageable = pageSettings.createPageRequest();

        return this.auctionRepo.findAll(pageable).toList();
    }

    public Auction add(String userId, Auction auction) {
        UUID userUUID = UUID.fromString(userId);
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new ObjectNotFoundException("user", userUUID));

        user.addAuction(auction);
        auction.initializeItems();

        return auctionRepo.save(auction);
    }

    public Auction update(String userId, Auction update, String auctionId) {

        UUID userUUID = UUID.fromString(userId);
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new ObjectNotFoundException("user", userUUID));

        UUID auctionUUID = UUID.fromString(auctionId);
        Auction oldAuction = auctionRepo.findById(auctionUUID)
                .orElseThrow(
                        () -> new ObjectNotFoundException("auction", auctionUUID)
                );

        authorizeUser(user, oldAuction);
        checkAuctionNotStarted(oldAuction);

        oldAuction.setName(update.getName());
        oldAuction.setStartTime(update.getStartTime());
        oldAuction.setEndTime(update.getEndTime());
        oldAuction.setInitialPrice(update.getInitialPrice());
        oldAuction.removeAllItems();
        oldAuction.addAllItems(getUpdatedItems(update));
        oldAuction.initializeItems();

        return this.auctionRepo.save(oldAuction);
    }

    private Set<Item> getUpdatedItems(Auction update) {
        Set<Item> updatedItems = new HashSet<>();
        for (Item auctionItem : update.getItems()) {
            Item item;
            if (auctionItem.getId() != null) {
                item = itemRepo.findById(auctionItem.getId())
                        .orElseThrow(() -> new ObjectNotFoundException("item", auctionItem.getId()));
            } else {
                item = new Item();
            }

            item.setName(auctionItem.getName());
            item.setDescription(auctionItem.getDescription());
            item.setLocation(auctionItem.getLocation());
            item.setExtras(auctionItem.getExtras());
            item.setLegitimacyProof(auctionItem.getLegitimacyProof());
            item.setImageUrls(auctionItem.getImageUrls());
            updatedItems.add(item);
        }
        return updatedItems;
    }

    private void authorizeUser(User user, Auction auction) {
        if (!user.getEmail().equals(auction.getOwnerEmail())) {
            throw new UnAuthorizedAuctionAccess();
        }
    }

    private void checkAuctionNotStarted(Auction auction) {
        if (auction.getStartTime().isBefore(Instant.now())) {
            throw new AuctionAlreadyBeganOrEndedException(auction.getId().toString());
        }
    }
}
