package me.vrishab.auction.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

public record AuctionItemUpdateDTO(
        String id,

        @NotBlank(message = "name is required")
        @Size(
                min = 2, max = 25,
                message = "minimum 2 character and maximum 25 characters."
        )
        String name,

        @NotBlank(message = "description is required")
        @Size(
                min = 2, max = 255,
                message = "minimum 2 character and maximum 255 characters."
        )
        String description,

        @NotBlank(message = "location is required")
        String location,

        @NotNull(message = "initial price is required")
        @PositiveOrZero(message = "Please provide a valid price")
        BigDecimal initialPrice,

        Set<String> imageUrls,

        String legitimacyProof,

        String extras
) {
}
