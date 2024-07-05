package me.vrishab.auction.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.User;

import java.util.UUID;

public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void addItemToWishlist(UUID userId, UUID itemId) {
        User user = em.find(User.class, userId);
        Item item = em.find(Item.class, itemId);
        if (user != null && item != null) {
            user.addFavouriteItem(item);
            em.merge(item);
        }
    }

    @Override
    @Transactional
    public void removeItemFromWishlist(UUID userId, UUID itemId) {
        User user = em.find(User.class, userId);
        Item item = em.find(Item.class, itemId);
        if (user != null && item != null) {
            user.removeFavouriteItem(item);
            em.merge(item);
        }
    }

    @Override
    @Transactional
    public void removeAllItemFromWishlist(UUID userId) {
        User user = em.find(User.class, userId);
        if (user != null) {
            user.getWishlist().forEach(item -> {
                removeItemFromWishlist(userId, item.getId());
            });
        }

    }
}
