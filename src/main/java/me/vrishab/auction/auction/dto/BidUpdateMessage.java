package me.vrishab.auction.auction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BidUpdateMessage(
        UUID auctionId,
        UUID itemId,
        BigDecimal currentBid,
        String buyer
) {
}
