package me.vrishab.auction.auction;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
            min = 2, max = 25,
            message = "minimum 2 character and maximum 25 characters."
    )
    private String name;

    @NotNull(message = "start date is required")
    @Future(message = "Please provide a future date")
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date startTime;

    @NotNull(message = "end date is required")
    @Future(message = "Please provide a future date")
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Date endTime;

    @NotNull(message = "initial price is required")
    @Positive(message = "Please provide a valid price")
    private Double initialPrice = 0.0;

    @NotNull
    @Setter(AccessLevel.NONE)
    @Positive(message = "Please provide a valid price")
    private Double currentBid;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL})
    @JoinColumn(name = "auctionId", nullable = false)
    private Set<Item> items = new HashSet<>();

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Please provide a valid email address")
    private String buyer;

}
