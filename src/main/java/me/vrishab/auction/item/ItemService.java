package me.vrishab.auction.item;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<Item> findAllPagination(int page, int size) {
        if (page > 0 && size > 0) return this.itemRepo.findAll(PageRequest.of(page - 1, size));
        else throw new ItemBadRequestException("Page number and size must be positive");
    }

    public List<Item> searchAllByName(String query) {
        return this.itemRepo.findAllByNameLikeIgnoreCase("%" + query + "%", Pageable.unpaged()).getContent();
    }

    public Page<Item> searchAllByName(String query, int page, int size) {
        if (page > 0 && size > 0)
            return this.itemRepo.findAllByNameLikeIgnoreCase("%" + query + "%", PageRequest.of(page - 1, size));
        else throw new ItemBadRequestException("Page number and size must be positive");
    }

    public List<Item> findAllByLocation(String location) {
        return this.itemRepo.findAllByLocation(location, Pageable.unpaged()).getContent();
    }

    public Page<Item> findAllByLocation(String location, int page, int size) {
        if (page > 0 && size > 0) return this.itemRepo.findAllByLocation(location, PageRequest.of(page - 1, size));
        else throw new ItemBadRequestException("Page number and size must be positive");
    }
}
