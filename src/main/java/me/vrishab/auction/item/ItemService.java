package me.vrishab.auction.item;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepo;

    @Autowired
    public ItemService(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public Item findById(String itemId) {
        return this.itemRepo.findById(UUID.fromString(itemId))
                .orElseThrow(
                        () -> new ItemNotFoundException(itemId)
                );
    }

    public List<Item> findAll() {
        return this.itemRepo.findAll();
    }
}
