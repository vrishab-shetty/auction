package me.vrishab.auction.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Constants;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Item {

    @Id
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    private String name;

    private String description;

    private String location;

    @ElementCollection
    @CollectionTable(
            name = Constants.IMAGE_TBL,
            joinColumns = @JoinColumn(name = Constants.ITEM_ID, nullable = false)
    )
    @Column(name = Constants.IMAGE_URL)
    private Set<String> imageUrls = new HashSet<>();

    private String legitimacyProof;

    private String extras;

    private BigDecimal initialPrice;

    private BigDecimal currentBid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = Constants.AUCTION_ID, nullable = false)
    private Auction auction;

    @ManyToMany
    @JoinTable(name = Constants.WISHLIST_TBL,
            joinColumns = @JoinColumn(name = Constants.ITEM_ID),
            inverseJoinColumns = @JoinColumn(name = Constants.USER_ID))
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<User> likedBy = new HashSet<>();

    @Column(nullable = false)
    private String seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(
            name = Constants.ITEM_BUYER_TBL,
            joinColumns = @JoinColumn(name = Constants.ITEM_ID),
            inverseJoinColumns = @JoinColumn(nullable = false)
    )
    private User buyer;

    public String getBuyerEmail() {
        if (buyer == null) return null;
        return this.buyer.getEmail();
    }

    public void addLikedUser(User user) {
        this.likedBy.add(user);
    }

    public void removeLikedUser(User user) {
        this.likedBy.remove(user);
    }

    public Long getPopularity() {
        return (long) likedBy.size();
    }
}
