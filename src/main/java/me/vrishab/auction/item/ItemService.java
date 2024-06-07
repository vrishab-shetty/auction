package me.vrishab.auction.item;

import jakarta.transaction.Transactional;
import me.vrishab.auction.item.ItemSpecification.ItemFilterParams;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static me.vrishab.auction.item.ItemSpecification.filterSpecification;

@Service
@Transactional
public class ItemService {

    private final ItemRepository itemRepo;

    @Autowired
    public ItemService(ItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public Item findById(String itemId) {
        UUID itemUUID = UUID.fromString(itemId);
        return this.itemRepo.findById(itemUUID)
                .orElseThrow(
                        () -> new ObjectNotFoundException("item", itemUUID)
                );
    }

    public List<Item> findAll(String query, String location, PageRequestParams pageParams) {
        Pageable pageable = Pageable.unpaged();

        if (pageParams != null && pageParams.getPageSize() != null && pageParams.getPageNum() != null)
            pageable = pageParams.createPageRequest();

        ItemFilterParams filter = new ItemFilterParams(query, location);
        return this.itemRepo.findAll(filterSpecification(filter), pageable).toList();
    }


}
