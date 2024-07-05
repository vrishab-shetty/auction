package me.vrishab.auction.item;

import jakarta.validation.Valid;
import me.vrishab.auction.item.converter.ItemToItemDTOConverter;
import me.vrishab.auction.item.dto.ItemDTO;
import me.vrishab.auction.system.PageRequestParams;
import me.vrishab.auction.system.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequestMapping("${api.endpoint.base-url}")
public class ItemController {

    private final ItemService itemService;
    private final ItemToItemDTOConverter itemToItemDTOConverter;

    @Autowired
    public ItemController(ItemService itemService, ItemToItemDTOConverter itemToItemDTOConverter) {
        this.itemService = itemService;
        this.itemToItemDTOConverter = itemToItemDTOConverter;
    }

    @GetMapping("/items/{itemId}")
    public Result findItemById(@PathVariable String itemId) {
        Item item = this.itemService.findById(itemId);
        ItemDTO itemDTO = this.itemToItemDTOConverter.convert(item);
        return new Result(true, "Find one Success", itemDTO);
    }

    @GetMapping("/items")
    public Result findAllItems(
            @ModelAttribute
            @Valid PageRequestParams pageParams,
            @RequestParam(required = false, name = "query") String name,
            @RequestParam(required = false, name = "location") String location
    ) {
        List<Item> items = itemService.findAll(name, location, pageParams);
        List<ItemDTO> itemDTOs = items.stream()
                .map(this.itemToItemDTOConverter::convert).toList();
        return new Result(true, "Find all items", itemDTOs);
    }

}
