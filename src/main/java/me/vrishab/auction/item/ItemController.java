package me.vrishab.auction.item;

import jakarta.validation.constraints.Positive;
import me.vrishab.auction.system.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
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
            @Positive(message = "Please provide positive value")
            @RequestParam(required = false) Integer pageNum,
            @Positive(message = "Please provide positive value")
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false, name = "query") String name,
            @RequestParam(required = false, name = "location") String location
    ) {
        if (pageNum != null && pageSize != null) {
            Page<Item> items;
            if (name != null) items = itemService.searchAllByName(name, pageNum, pageSize);
            else if (location != null) items = itemService.findAllByLocation(location, pageNum, pageSize);
            else items = itemService.findAllPagination(pageNum, pageSize);
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
