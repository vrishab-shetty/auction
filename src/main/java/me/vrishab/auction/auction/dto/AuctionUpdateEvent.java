package me.vrishab.auction.auction.dto;

import me.vrishab.auction.user.dto.UserSummaryDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionUpdateEvent(
        UUID auctionId,
        UUID itemId,
        BigDecimal currentPrice,
        UserSummaryDTO buyer
) {}
