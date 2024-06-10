package me.vrishab.auction.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BidRequestDTO(
        @NotNull(message = "initial price is required")
        @PositiveOrZero(message = "Please provide a valid price")
        Double bidAmount
) {
}
