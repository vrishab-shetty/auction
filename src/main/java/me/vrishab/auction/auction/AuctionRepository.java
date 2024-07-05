package me.vrishab.auction.auction;

import me.vrishab.auction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    List<Auction> findByUser(User user);

    void deleteByUser(User user);
}
