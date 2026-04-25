package me.vrishab.auction.auction;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class AuctionSpecification {

    public static Specification<Auction> isActive() {
        return (root, query, criteriaBuilder) -> {
            Instant now = Instant.now();
            return criteriaBuilder.and(
                    criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), now),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), now)
            );
        };
    }

    public static Specification<Auction> isScheduled() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("startTime"), Instant.now());
    }

    public static Specification<Auction> isEnded() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("endTime"), Instant.now());
    }

}
