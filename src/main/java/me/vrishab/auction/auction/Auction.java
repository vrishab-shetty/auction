package me.vrishab.auction.auction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.utils.Constants;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Auction {

    @Id
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    private String name;

    private Instant startTime;

    private Instant endTime;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL},
            mappedBy = "auction")
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Set<Item> items = new HashSet<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinTable(
            name = Constants.USER_AUCTION_TBL,
            joinColumns = @JoinColumn(name = Constants.AUCTION_ID, unique = true),
            inverseJoinColumns = @JoinColumn(name = Constants.USER_ID)
    )
    @Setter(AccessLevel.NONE)
    private User user;

    public String getOwnerEmail() {
        return user.getEmail();
    }

    public void removeAllItems() {
        this.items.forEach(item -> item.setAuction(null));
        this.items.clear();
    }

    public void addAllItems(Set<Item> items) {
        this.items.addAll(items);
        this.items.forEach(item -> {
            item.setAuction(this);
            if (user != null) item.setSeller(user.getEmail());
        });
    }

    public Set<Item> getItems() {
        return Collections.unmodifiableSet(this.items);
    }

    public void setUser(User user) {
        this.user = user;
        this.items.forEach(item -> item.setSeller(this.user.getEmail()));
    }
}
