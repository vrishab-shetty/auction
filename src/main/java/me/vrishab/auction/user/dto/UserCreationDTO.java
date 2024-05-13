package me.vrishab.auction.user.dto;

import lombok.NonNull;

public record UserCreationDTO (
        @NonNull
        String name,
        @NonNull
        String password,
        String description,
        @NonNull
        String email,
        @NonNull
        String contact,
        @NonNull
        Boolean enabled
) {
}
