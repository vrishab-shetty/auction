package me.vrishab.auction.auction.dto;

import lombok.NonNull;
import me.vrishab.auction.item.dto.AuctionItemDTO;

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
        List<AuctionItemDTO> items,

        @NonNull
        String user
) {
}