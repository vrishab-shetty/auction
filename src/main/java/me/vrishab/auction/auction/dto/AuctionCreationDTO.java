package me.vrishab.auction.auction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import me.vrishab.auction.item.dto.ItemCreationDTO;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AuctionCreationDTO(
        @NotBlank(message = "name is required")
        @Size(
                min = 2, max = 25,
                message = "minimum 2 character and maximum 25 characters."
        )
        String name,

        @NotNull(message = "start date is required")
        @Future(message = "Please provide a future date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant startTime,

        @NotNull(message = "end date is required")
        @Future(message = "Please provide a future date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant endTime,

        @NotNull(message = "initial price is required")
        @PositiveOrZero(message = "Please provide a valid price")
        BigDecimal initialPrice,

        @NotNull(message = "At least one item is required")
        @Size(
                min = 1,
                message = "auction needs at least one item"
        )
        @Valid List<ItemCreationDTO> items
) {
}
