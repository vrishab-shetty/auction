package me.vrishab.auction.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String description;

    private String location;

    @ElementCollection
    @CollectionTable(
            name = "IMAGE",
            joinColumns = @JoinColumn(name = "ITEM_ID")
    )
    @Column(name = "IMAGE_URL")
    private Set<String> imageUrls = new HashSet<>();

    private String legitimacyProof;

    private String extras;

    @Column(insertable = false, updatable = false, nullable = false)
    private UUID auctionId;

    @Column(nullable = false)
    private String seller;

}
