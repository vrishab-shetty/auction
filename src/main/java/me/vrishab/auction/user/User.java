package me.vrishab.auction.user;

import jakarta.persistence.*;
import lombok.*;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String description;

    private String password;

    private Boolean enabled;

    @Column(unique = true)
    private String email;

    private String contact;

    @ManyToMany
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "itemId"))
    @Getter(AccessLevel.NONE)
    private Set<Item> wishList = new HashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "user")
    @Getter(AccessLevel.NONE)
    private Set<Auction> auctions = new HashSet<>();

    public Set<Item> getWishList() {
        return Collections.unmodifiableSet(this.wishList);
    }

    public Set<Auction> getAuctions() {
        return Collections.unmodifiableSet(this.auctions);
    }

    public void addFavouriteItem(@NonNull Item item) {
        this.wishList.add(item);
    }

    public void removeFavouriteItem(@NonNull Item item) {
        this.wishList.remove(item);
    }

    public void addAuction(@NonNull Auction auction) {
        this.auctions.add(auction);
        auction.setUser(this);
    }

    public void removeAuction(@NonNull Auction auction) {
        auction.setUser(null);
        this.auctions.remove(auction);
    }
}