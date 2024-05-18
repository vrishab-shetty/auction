package me.vrishab.auction.item;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    Page<Item> findAllByNameLikeIgnoreCase(@NonNull String name, Pageable pageable);

    Page<Item> findAllByLocation(@NonNull String location, Pageable pageable);
}
