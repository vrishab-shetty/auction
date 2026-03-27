package me.vrishab.auction.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a real-time update event for an auction.
 */
public record AuctionUpdateEvent(
    String type,
    String auctionId,
    String itemId,
    BigDecimal bidAmount,
    String bidderId,
    Instant timestamp,
    String status
) {
    public static AuctionUpdateEvent bidPlaced(String auctionId, String itemId, BigDecimal bidAmount, String bidderId) {
        return new AuctionUpdateEvent("BID_PLACED", auctionId, itemId, bidAmount, bidderId, Instant.now(), "OPEN");
    }

    public static AuctionUpdateEvent auctionClosed(String auctionId) {
        return new AuctionUpdateEvent("AUCTION_CLOSED", auctionId, null, null, null, Instant.now(), "CLOSED");
    }
}
