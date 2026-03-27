package me.vrishab.auction.auction;

import me.vrishab.auction.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    List<Auction> findByUser(User user);

    void deleteByUser(User user);

    @Override
    @EntityGraph(attributePaths = {"items"})
    Page<Auction> findAll(Pageable pageable);

    List<Auction> findByEndTimeBetween(Instant start, Instant end);
}
