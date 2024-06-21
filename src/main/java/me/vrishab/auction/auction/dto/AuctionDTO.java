package me.vrishab.auction.auction.dto;

import lombok.NonNull;
import me.vrishab.auction.item.dto.AuctionItemDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuctionDTO(
        @NonNull
        UUID id,

        @NonNull
        String name,

        @NonNull
        Instant startTime,

        @NonNull
        Instant endTime,

        @NonNull
        BigDecimal initialPrice,

        BigDecimal currentBid,

        @NonNull
        List<AuctionItemDTO> items,

        String buyer,

        @NonNull
        String user
) {
}