package me.vrishab.auction.item;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Item implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "name is required")
    @Size(
            min = 2, max = 25,
            message = "minimum 2 character and maximum 25 characters."
    )
    private String name;

    @NotBlank(message = "description is required")
    @Size(
            min = 2, max = 255,
            message = "minimum 2 character and maximum 255 characters."
    )
    private String description;

    @NotBlank(message = "location is required")
    private String location;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> imageUrls = new HashSet<>();

    private String legitimacyProof;

    private String extras;

    @NotNull
    @Column(insertable = false, updatable = false, nullable = false)
    private UUID auctionId;

    @NotNull
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    @Column(nullable = false)
    private String seller;

}
