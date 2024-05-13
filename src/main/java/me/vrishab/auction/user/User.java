package me.vrishab.auction.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    private String name;

    private String description;

    @NotBlank(message = "password is required")
    private String password;

    private boolean enabled;

    @NotBlank(message = "email is required")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")

    private String email;

    @NotBlank(message = "contact detail is required")
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

    public void addAuctions(@NonNull Auction auction) {
        this.auctions.add(auction);
    }

    public void removeAuction(@NonNull Auction auction) {
        this.auctions.remove(auction);
    }
}