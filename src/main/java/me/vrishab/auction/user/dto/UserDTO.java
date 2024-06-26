package me.vrishab.auction.user.dto;

import lombok.NonNull;
import me.vrishab.auction.user.model.Address;

import java.util.UUID;

public record UserDTO(
        @NonNull
        UUID id,
        @NonNull
        String name,
        String description,
        @NonNull
        String username,
        @NonNull
        String contact,
        @NonNull
        Boolean enabled,
        @NonNull
        Address homeAddress
) {
}
