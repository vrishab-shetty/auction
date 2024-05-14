package me.vrishab.auction.auction;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import me.vrishab.auction.item.Item;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Auction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "name is required")
    @Size(
            min = 2,
            max = 255,
            message = "minimum 2 character and maximum 255 characters."
    )
    private String name;

    @NotNull(message = "Please provide a start date")
    @Future(message = "Please provide a future date")
    @DateTimeFormat(iso = ISO.DATE_TIME)
    @NonNull
    private Date startTime;

    @NotNull(message = "Please provide a end date")
    @Future(message = "Please provide a future date")
    @DateTimeFormat(iso = ISO.DATE_TIME)
    @NonNull
    private Date endTime;

    @NotNull(message = "Please provide a initial price")
    @NonNull
    private Double initialPrice;

    @NonNull
    @Setter(AccessLevel.NONE)
    private Double currentBid;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL})
    @JoinColumn(name = "auctionId", nullable = false)
    private Set<Item> items = new HashSet<>();

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String buyer;

}
