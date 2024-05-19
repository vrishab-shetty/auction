package me.vrishab.auction.item;

import me.vrishab.auction.system.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items/{itemId}")
    public Result findItemById(@PathVariable String itemId) {
        Item item = this.itemService.findById(itemId);
        return new Result(true, "Find one Success", item);
    }

    @GetMapping("/items")
    public Result findAllItems(
            @RequestParam(required = false, name = "pageNum") Integer page,
            @RequestParam(required = false, name = "pageSize") Integer size,
            @RequestParam(required = false, name = "query") String name,
            @RequestParam(required = false, name = "location") String location
    ) {
        if (page != null && size != null) {
            Page<Item> items;
            if (name != null) items = itemService.searchAllByName(name, page, size);
            else if (location != null) items = itemService.findAllByLocation(location, page, size);
            else items = itemService.findAllPagination(page, size);
            return new Result(true, "Find all items", items.get());
        } else if (name != null) {
            List<Item> items = itemService.searchAllByName(name);
            return new Result(true, "Find all items with name containing " + name, items);
        } else if (location != null) {
            List<Item> items = itemService.findAllByLocation(location);
            return new Result(true, "Find all items with location " + location, items);
        }
        return new Result(true, "Find all items", itemService.findAll());
    }

}
