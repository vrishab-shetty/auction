package me.vrishab.auction.auction;

import jakarta.transaction.Transactional;
import me.vrishab.auction.auction.AuctionException.*;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemException;
import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AuctionService {

    private final AuctionRepository auctionRepo;

    private final UserRepository userRepo;

    private final ItemRepository itemRepo;

    private final AuctionException auctionException;

    private final ItemException itemException;

    public AuctionService(AuctionRepository auctionRepo, UserRepository userRepo, ItemRepository itemRepo) {
        this.auctionRepo = auctionRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.auctionException = new AuctionException();
        this.itemException = new ItemException();
    }

    public Auction findById(String id) {
        UUID auctionUUID = UUID.fromString(id);
        return this.auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));
    }

    public List<Auction> findAll(PageRequestParams pageSettings) {
        Pageable pageable = Pageable.unpaged();
        if (pageSettings != null && pageSettings.isValid())
            pageable = pageSettings.createPageRequest();

        return this.auctionRepo.findAll(pageable).toList();
    }

    public Auction add(String userId, Auction auction) {
        UUID userUUID = UUID.fromString(userId);
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new UserNotFoundByIdException(userUUID));

        user.addAuction(auction);
        auction.initializeItems();

        return auctionRepo.save(auction);
    }

    public Auction update(String userId, Auction update, String auctionId) {

        UUID auctionUUID = UUID.fromString(auctionId);
        Auction oldAuction = auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

        authorizedUser(UUID.fromString(userId), oldAuction);
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

    public void delete(String userId, String auctionId) {
        UUID auctionUUID = UUID.fromString(auctionId);
        Auction oldAuction = auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

        authorizedUser(UUID.fromString(userId), oldAuction);
        checkAuctionNotStarted(oldAuction);

        this.auctionRepo.deleteById(auctionUUID);
    }

    public Auction bid(String userId, String auctionId, Double bidAmount) {

        UUID auctionUUID = UUID.fromString(auctionId);
        Auction auction = auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

        User user = unauthorizedUser(UUID.fromString(userId), auction);
        checkAuctionInBidingPhase(auction);
        checkAuctionBidAmount(auction, bidAmount);

        auction.setCurrentBid(bidAmount);
        auction.setBuyer(user.getEmail());

        return this.auctionRepo.save(auction);
    }

    private void checkAuctionBidAmount(Auction auction, Double bidAmount) {

        Double currentPrice = Optional.ofNullable(auction.getCurrentBid())
                .orElse(auction.getInitialPrice());

        if (bidAmount <= currentPrice) {
            throw new InvalidBidAmountException();
        }
    }

    private void checkAuctionInBidingPhase(Auction auction) {
        Instant now = Instant.now();
        ;
        boolean isAuctionStarted = auction.getStartTime().isBefore(now);
        boolean isAuctionEnded = auction.getEndTime().isBefore(now);

        if (!isAuctionStarted || isAuctionEnded) {
            throw new AuctionForbiddenBidingPhaseException(auction.getId());
        }
    }

    private Set<Item> getUpdatedItems(Auction update) {
        Set<Item> updatedItems = new HashSet<>();
        for (Item auctionItem : update.getItems()) {
            Item item;
            if (auctionItem.getId() != null) {
                item = itemRepo.findById(auctionItem.getId())
                        .orElseThrow(() -> new ItemNotFoundByIdException(auctionItem.getId()));
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

    private void authorizedUser(UUID userUUID, Auction auction) {
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new ItemNotFoundByIdException(userUUID));

        if (!user.getEmail().equals(auction.getOwnerEmail())) {
            throw new UnauthorizedAuctionAccess(false);
        }
    }

    private User unauthorizedUser(UUID userUUID, Auction auction) {
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new ItemNotFoundByIdException(userUUID));

        if (user.getEmail().equals(auction.getOwnerEmail())) {
            throw new UnauthorizedAuctionAccess(true);
        }

        return user;
    }

    private void checkAuctionNotStarted(Auction auction) {
        if (auction.getStartTime().isBefore(Instant.now())) {
            throw new AuctionForbiddenUpdateException(auction.getId());
        }
    }
}
