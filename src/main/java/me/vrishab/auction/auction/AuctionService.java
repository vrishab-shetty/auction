package me.vrishab.auction.auction;

import me.vrishab.auction.auction.AuctionException.*;
import me.vrishab.auction.auction.exception.ConcurrentBidException;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserRepository;
import me.vrishab.auction.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.BidUpdateMessage;
import me.vrishab.auction.system.configuration.RedisPubSubConfig;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuctionService {

    private static final String BIDS_KEY_PREFIX = "auction:item:bids:";
    private final AuctionRepository auctionRepo;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    @Value("${auction.bidding.lock-timeout-seconds}")
    private long lockTimeoutSeconds;

    public AuctionService(AuctionRepository auctionRepo, UserRepository userRepo,
                          ItemRepository itemRepo, RedisTemplate<String, String> redisTemplate,
                          TransactionTemplate transactionTemplate, ObjectMapper objectMapper) {
        this.auctionRepo = auctionRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.redisTemplate = redisTemplate;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Auction findById(String id) {
        UUID auctionUUID = UUID.fromString(id);
        return this.auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));
    }

    @Transactional(readOnly = true)
    public Page<Auction> findAll(PageRequestParams pageSettings) {
        Pageable pageable = Pageable.unpaged();
        if (pageSettings != null && pageSettings.isValid())
            pageable = pageSettings.createPageRequest();

        return this.auctionRepo.findAll(pageable);
    }

    @Transactional
    public Auction add(String userId, Auction auction) {
        UUID userUUID = UUID.fromString(userId);
        User user = userRepo.findById(userUUID)
                .orElseThrow(() -> new UserNotFoundByIdException(userUUID));

        auction.setUser(user);

        return auctionRepo.save(auction);
    }

    @Transactional
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

    @Transactional
    public void delete(String userId, String auctionId) {
        UUID auctionUUID = UUID.fromString(auctionId);
        Auction oldAuction = auctionRepo.findById(auctionUUID)
                .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

        authorizedUser(UUID.fromString(userId), oldAuction);
        checkAuctionNotStarted(oldAuction);

        this.auctionRepo.deleteById(auctionUUID);
    }

    public Item bid(String userId, String auctionId, String itemId, BigDecimal bidAmount) {
        String lockKey = "lock:item:" + itemId;
        String lockId = UUID.randomUUID().toString();

        try {
            // 1. Acquire Redis Lock (with configurable timeout)
            boolean locked = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(lockKey, lockId, Duration.ofSeconds(lockTimeoutSeconds))
            );
            if (!locked) throw new ConcurrentBidException();

            // 2. Execute Database Transaction
            return transactionTemplate.execute(status -> {
                UUID auctionUUID = UUID.fromString(auctionId);
                Auction auction = auctionRepo.findById(auctionUUID)
                        .orElseThrow(() -> new AuctionNotFoundByIdException(auctionUUID));

                UUID itemUUID = UUID.fromString(itemId);
                Item item = itemRepo.findById(itemUUID)
                        .orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));

                if (!auction.getItems().contains(item)) {
                    throw new AuctionItemNotFoundException(auctionUUID, item.getId());
                }

                User user = getAndValidateBidder(UUID.fromString(userId), auction);
                checkAuctionInBidingPhase(auction);
                checkAuctionBidAmount(item, bidAmount);

                // Update Item state
                item.setCurrentBid(bidAmount);
                item.setBuyer(user);
                Item savedItem = this.itemRepo.save(item);

                // 3. Register Redis Update Post-Commit
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        String redisKey = BIDS_KEY_PREFIX + itemId;
                        // Use a scale to maintain precision for currency in double-based ZSet
                        double score = bidAmount.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                        redisTemplate.opsForZSet().add(redisKey, userId, score);

                        try {
                            BidUpdateMessage message = new BidUpdateMessage(auctionUUID, itemUUID, bidAmount, savedItem.getBuyer().getEmail());
                            String jsonMessage = objectMapper.writeValueAsString(message);
                            redisTemplate.convertAndSend(RedisPubSubConfig.BID_UPDATES_CHANNEL, jsonMessage);
                        } catch (JsonProcessingException e) {
                            System.err.println("Failed to serialize BidUpdateMessage: " + e.getMessage());
                        }
                    }
                });

                return savedItem;
            });
        } finally {
            // 4. Release Redis Lock
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class),
                    Collections.singletonList(lockKey), lockId);
        }
    }

    private void checkAuctionBidAmount(Item item, BigDecimal bidAmount) {
        if (item.getCurrentBid() == null) {
            // First bid must be at least the initial price
            if (bidAmount.compareTo(item.getInitialPrice()) < 0) {
                throw new InvalidBidAmountException();
            }
        } else {
            // Subsequent bids must be strictly higher than the current bid
            if (bidAmount.compareTo(item.getCurrentBid()) <= 0) {
                throw new InvalidBidAmountException();
            }
        }
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

    private User getAndValidateBidder(UUID userUUID, Auction auction) {
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
