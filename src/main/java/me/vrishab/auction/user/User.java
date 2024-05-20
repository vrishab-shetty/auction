package me.vrishab.auction.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "name is required")
    @Size(
            min = 2, max = 25,
            message = "minimum 2 character and maximum 25 characters."
    )
    private String name;

    @NotNull
    @Size(
            min = 2, max = 255,
            message = "minimum 2 character and maximum 255 characters."
    )
    private String description;

    @NotBlank(message = "password is required")
    @Size(
            min = 8, max = 64,
            message = "minimum 8 character and maximum 64 characters"
    )
    private String password;

    @NotNull
    private boolean enabled = true;

    @NotBlank(message = "email is required")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "contact info is required")
    @Size(min = 10, max = 10, message = "Please provide a valid phone number")
    private String contact;

    @ManyToMany
    @JoinTable(name = "wishlist",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "itemId"))
    @Getter(AccessLevel.NONE)
    private Set<Item> wishList = new HashSet<>();

    @OneToMany
    @JoinColumn(name = "ownerId")
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
    }

    public void removeAuction(@NonNull Auction auction) {
        this.auctions.remove(auction);
    }
}