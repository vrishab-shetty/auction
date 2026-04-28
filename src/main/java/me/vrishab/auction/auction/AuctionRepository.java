package me.vrishab.auction.auction;

import me.vrishab.auction.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID>, JpaSpecificationExecutor<Auction> {
    @EntityGraph(attributePaths = {"user"})
    List<Auction> findByUser(User user);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Auction> findById(UUID id);

    void deleteByUser(User user);

    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Auction> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user"})
    @NonNull
    Page<Auction> findAll(@NonNull Specification<Auction> spec, @NonNull Pageable pageable);
}
