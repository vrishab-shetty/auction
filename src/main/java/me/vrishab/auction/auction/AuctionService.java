package me.vrishab.auction.auction;

import jakarta.transaction.Transactional;
import me.vrishab.auction.auction.AuctionException.*;
import me.vrishab.auction.auction.exception.ConcurrentBidException;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
public class AuctionService {

    private static final String BIDS_KEY_PREFIX = "auction:item:bids:";
    private final AuctionRepository auctionRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final RedisTemplate<String, String> redisTemplate;


    public AuctionService(AuctionRepository auctionRepo, UserRepository userRepo,
                          ItemRepository itemRepo, RedisTemplate<String, String> redisTemplate) {
        this.auctionRepo = auctionRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.redisTemplate = redisTemplate;
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

        auction.setUser(user);

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
        Set<Item> updated = getUpdatedItems(oldAuction, update);
        oldAuction.removeAllItems();
        oldAuction.addAllItems(updated);

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

    public Item bid(String userId, String auctionId, String itemId, BigDecimal bidAmount) {
        UUID auctionUUID = UUID.fromString(auctionId);
        Auction auction = auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

        UUID itemUUID = UUID.fromString(itemId);
        Item item = itemRepo.findById(itemUUID)
                .orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));
        if (!auction.getItems().contains(item)) {
            throw new AuctionItemNotFoundException(auctionUUID, item.getId());
        }

        User user = unauthorizedUser(UUID.fromString(userId), auction);
        checkAuctionInBidingPhase(auction);
        checkAuctionBidAmount(item, bidAmount);

        // Distributed lock for concurrent bid handling
        String lockKey = "lock:item:" + itemId;
        String lockId = UUID.randomUUID().toString();
        try {
            // Try to acquire lock with 5 second timeout
            boolean locked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, lockId, Duration.ofSeconds(5))
            );
            if (!locked) throw new ConcurrentBidException();

            // Track bid in Redis sorted set
            String redisKey = BIDS_KEY_PREFIX + itemId;
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            zSetOps.add(redisKey, userId, bidAmount.doubleValue());

            // Get current highest bid
            Double highestBid = zSetOps.score(redisKey, userId);
            item.setCurrentBid(BigDecimal.valueOf(highestBid));
            item.setBuyer(user);

            return this.itemRepo.save(item);
        } finally {
            // Release the lock if it was acquired by this thread
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(lockKey), lockId);
        }
    }

    private void checkAuctionBidAmount(Item item, BigDecimal bidAmount) {

        BigDecimal currentPrice = Optional.ofNullable(item.getCurrentBid())
                .orElse(item.getInitialPrice());

        if (bidAmount.compareTo(currentPrice) < 0) throw new InvalidBidAmountException();
    }

    private void checkAuctionInBidingPhase(Auction auction) {
        Instant now = Instant.now();
        boolean isAuctionStarted = auction.getStartTime().isBefore(now);
        boolean isAuctionEnded = auction.getEndTime().isBefore(now);

        if (!isAuctionStarted || isAuctionEnded) {
            throw new AuctionForbiddenBidingPhaseException(auction.getId());
        }
    }

    private Set<Item> getUpdatedItems(Auction oldAuction, Auction update) {
        Set<Item> updatedItems = new HashSet<>();
        for (Item auctionItem : update.getItems()) {
            Item item;
            if (auctionItem.getId() != null) {
                item = itemRepo.findById(auctionItem.getId())
                        .orElseThrow(() -> new ItemNotFoundByIdException(auctionItem.getId()));
                if (!oldAuction.getItems().contains(item)) {
                    throw new AuctionItemNotFoundException(oldAuction.getId(), item.getId());
                }
            } else {
                item = new Item();
            }

            item.setName(auctionItem.getName());
            item.setDescription(auctionItem.getDescription());
            item.setLocation(auctionItem.getLocation());
            item.setExtras(auctionItem.getExtras());
            item.setLegitimacyProof(auctionItem.getLegitimacyProof());
            item.setInitialPrice(auctionItem.getInitialPrice());
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
