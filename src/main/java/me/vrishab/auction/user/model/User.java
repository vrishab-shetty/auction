package me.vrishab.auction.user.model;

import jakarta.persistence.*;
import lombok.*;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor
@Table(name = "\"USER\"")
public class User {
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

    private Address homeAddress;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<BillingDetails> billingDetails = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "itemId"))
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Item> wishlist = new HashSet<>();

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "user")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Auction> auctions = new HashSet<>();

    public Set<Item> getWishlist() {
        return Collections.unmodifiableSet(this.wishlist);
    }

    public Set<Auction> getAuctions() {
        return Collections.unmodifiableSet(this.auctions);
    }

    public void addFavouriteItem(@NonNull Item item) {
        this.wishlist.add(item);
    }

    public void removeFavouriteItem(@NonNull Item item) {
        this.wishlist.remove(item);
    }

    public void addAuction(@NonNull Auction auction) {
        this.auctions.add(auction);
        auction.setUser(this);
    }

    public String getHomeZipCode() {
        return this.homeAddress.getZipcode();
    }

    public String getHomeStreet() {
        return this.homeAddress.getStreet();
    }

    public String getHomeCity() {
        return this.homeAddress.getCity();
    }

    public String getHomeCountry() { return this.homeAddress.getCountry(); }

    public Set<BillingDetails> getBillingDetails() {
        return Collections.unmodifiableSet(billingDetails);
    }

    public void addBillingDetail(BillingDetails billingDetail) {
        billingDetails.add(billingDetail);
        billingDetail.setUser(this);
    }
}