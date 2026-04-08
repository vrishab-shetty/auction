package me.vrishab.auction.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID>, JpaSpecificationExecutor<Item> {

    @Query("SELECT i FROM Item i ORDER BY SIZE(i.likedBy) DESC, i.id ASC")
    Page<Item> findAllOrderByPopularity(Pageable pageable);

}
