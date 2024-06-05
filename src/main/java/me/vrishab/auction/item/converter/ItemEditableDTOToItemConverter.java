package me.vrishab.auction.item.converter;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.ItemEditableDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemEditableDTOToItemConverter implements Converter<ItemEditableDTO, Item> {

    @Override
    public Item convert(ItemEditableDTO source) {

        Item item = new Item();

        item.setName(source.name());
        item.setDescription(source.description());
        item.setLocation(source.location());
        item.setImageUrls(source.imageUrls());
        item.setLegitimacyProof(source.legitimacyProof());
        item.setExtras(source.extras());

        return item;
    }
}
