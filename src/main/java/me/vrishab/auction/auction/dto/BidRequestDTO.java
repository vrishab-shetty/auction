package me.vrishab.auction.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BidRequestDTO(
        @NotNull(message = "bid amount is required")
        @PositiveOrZero(message = "Please provide a valid price")
        Double bidAmount
) {
}
