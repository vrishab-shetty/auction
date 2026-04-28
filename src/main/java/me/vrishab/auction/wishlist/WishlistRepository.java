package me.vrishab.auction.wishlist;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user = :user AND w.item = :item")
    void deleteByUserAndItem(@Param("user") User user, @Param("item") Item item);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.item IN (SELECT i FROM Item i WHERE i.auction.user = :user)")
    void deleteByItemSeller(@Param("user") User user);

    @Query("SELECT i FROM Wishlist w JOIN w.item i " +
            "LEFT JOIN FETCH i.buyer " +
            "LEFT JOIN FETCH i.auction a " +
            "LEFT JOIN FETCH a.user " +
            "WHERE w.user = :user")
    List<Item> findItemsByUser(@Param("user") User user);
}
