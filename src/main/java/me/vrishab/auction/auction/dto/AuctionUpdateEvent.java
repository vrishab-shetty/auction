package me.vrishab.auction.auction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionUpdateEvent(
        UUID auctionId,
        UUID itemId,
        BigDecimal currentPrice,
        String buyerName
) {}
