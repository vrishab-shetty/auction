package me.vrishab.auction.wishlist;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Constants;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Table(name = Constants.WISHLIST_TBL,
        uniqueConstraints = @UniqueConstraint(columnNames = {Constants.ITEM_ID, Constants.USER_ID}))
public class Wishlist {

    @Id
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Constants.USER_ID, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Constants.ITEM_ID, nullable = false)
    private Item item;

    private Instant addedAt;

    public Wishlist(User user, Item item) {
        this.user = user;
        this.item = item;
        this.addedAt = Instant.now();
    }
}
