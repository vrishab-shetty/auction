package me.vrishab.auction.auction;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.User;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Auction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private Instant startTime;

    private Instant endTime;

    private Double initialPrice;

    @PositiveOrZero(message = "Please provide a valid price")
    private Double currentBid;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL})
    @JoinColumn(name = "auctionId", nullable = false)
    private Set<Item> items = new HashSet<>();

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String buyer;

    @ManyToOne
    private User user;

    public String getOwnerEmail() {
        return user.getEmail();
    }

    public void initializeItems() {
        if (user == null) {
            throw new IllegalStateException("User must be set before initializing items");
        }
        items.forEach(item -> {
            item.setAuctionId(this.id);
            item.setSeller(user.getEmail());
        });
    }

    public void removeAllItems() {
        this.items.forEach(item -> item.setAuctionId(null));
        this.items.clear();
    }

    public void addAllItems(Set<Item> items) {
        this.items.addAll(items);
        initializeItems();
    }
}
