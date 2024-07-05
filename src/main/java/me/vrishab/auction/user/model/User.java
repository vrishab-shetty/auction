package me.vrishab.auction.user.model;

import jakarta.persistence.*;
import lombok.*;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.utils.Constants;

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
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    private String name;

    private String description;

    private String password;

    private Boolean enabled;

    @Column(unique = true)
    private String email;

    private String contact;

    private Address homeAddress;

    @ManyToMany(mappedBy = "likedBy")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Set<Item> wishlist = new HashSet<>();

    public Set<Item> getWishlist() {
        return Collections.unmodifiableSet(this.wishlist);
    }

    public void addFavouriteItem(@NonNull Item item) {
        this.wishlist.add(item);
        item.addLikedUser(this);
    }

    public void removeFavouriteItem(@NonNull Item item) {
        this.wishlist.remove(item);
        item.removeLikedUser(this);
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

    public String getHomeCountry() {
        return this.homeAddress.getCountry();
    }

}