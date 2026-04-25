package me.vrishab.auction.bid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BidRepository extends JpaRepository<Bid, UUID> {

    @EntityGraph(attributePaths = {"bidder", "item"})
    Page<Bid> findByItemIdOrderByPlacedAtDesc(UUID itemId, Pageable pageable);
}
