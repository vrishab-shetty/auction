package me.vrishab.auction.user.dto;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public record UserDTO(
        UUID id,
        @NonNull
        String name,
        String description,
        @NonNull
        String email,
        @NonNull
        String contact,
        @NonNull
        Boolean enabled
) {
}
