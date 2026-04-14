package me.vrishab.auction.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {

    @Override
    @EntityGraph(attributePaths = {"buyer", "auction.user"})
    Optional<Item> findById(UUID id);

    @EntityGraph(attributePaths = {"buyer", "auction.user"})
    @Query("SELECT i FROM Item i ORDER BY SIZE(i.likedBy) DESC, i.id ASC")
    Page<Item> findAllOrderByPopularity(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"buyer", "auction.user"})
    Page<Item> findAll(@Nullable Specification<Item> spec, Pageable pageable);

}
