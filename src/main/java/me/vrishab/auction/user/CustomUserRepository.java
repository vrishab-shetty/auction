package me.vrishab.auction.user;

import java.util.UUID;

public interface CustomUserRepository {
    void addItemToWishlist(UUID userId, UUID itemId);

    void removeItemFromWishlist(UUID userId, UUID itemId);

    void removeAllItemFromWishlist(UUID userId);
}
