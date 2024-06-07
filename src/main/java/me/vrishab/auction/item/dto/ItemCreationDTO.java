package me.vrishab.auction.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ItemCreationDTO(
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

        Set<String> imageUrls,

        String legitimacyProof,

        String extras
) {
}
