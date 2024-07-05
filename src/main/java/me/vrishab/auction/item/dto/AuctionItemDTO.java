package me.vrishab.auction.item.dto;

import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record AuctionItemDTO(
        @NonNull
        UUID id,

        @NonNull
        String name,

        @NonNull
        String description,

        @NonNull
        String location,

        @NonNull
        BigDecimal initialPrice,

        BigDecimal currentBid,

        Set<String> imageUrls,

        String legitimacyProof,

        String extras,

        String buyer
) {
}
