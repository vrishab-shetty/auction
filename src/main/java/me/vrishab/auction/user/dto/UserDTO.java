package me.vrishab.auction.user.dto;

import lombok.NonNull;

import java.util.UUID;

public record UserDTO(
        UUID id,
        @NonNull
        String name,
        String description,
        @NonNull
        String username,
        @NonNull
        String contact,
        @NonNull
        Boolean enabled
) {
}
