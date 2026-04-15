package me.vrishab.auction.item.dto;

import lombok.NonNull;
import me.vrishab.auction.user.dto.UserSummaryDTO;

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

        BigDecimal currentBid,

        Set<String> imageUrls,

        UserSummaryDTO buyer
) {
}
