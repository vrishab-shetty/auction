package me.vrishab.auction.auction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private Instant startTime;

    private Instant endTime;

    private BigDecimal initialPrice;

    private BigDecimal currentBid;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL})
    @JoinColumn(name = "auctionId", nullable = false)
    private Set<Item> items = new HashSet<>();

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
