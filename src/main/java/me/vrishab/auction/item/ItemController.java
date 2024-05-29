package me.vrishab.auction.item;

import jakarta.validation.Valid;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("${api.endpoint.base-url}")
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
            @ModelAttribute
            @Valid PageRequestParams pageParams,
            @RequestParam(required = false, name = "query") String name,
            @RequestParam(required = false, name = "location") String location
    ) {
        return new Result(true, "Find all items", itemService.findAll(name, location, pageParams));
    }

}
