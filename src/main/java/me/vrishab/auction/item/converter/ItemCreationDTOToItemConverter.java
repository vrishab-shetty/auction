package me.vrishab.auction.item.converter;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.ItemCreationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemCreationDTOToItemConverter implements Converter<ItemCreationDTO, Item> {

    @Override
    public Item convert(ItemCreationDTO source) {

        Item item = new Item();

        item.setName(source.name());
        item.setDescription(source.description());
        item.setLocation(source.location());
        item.setImageUrls(source.imageUrls());
        item.setLegitimacyProof(source.legitimacyProof());
        item.setInitialPrice(source.initialPrice());
        item.setExtras(source.extras());

        return item;
    }
}
