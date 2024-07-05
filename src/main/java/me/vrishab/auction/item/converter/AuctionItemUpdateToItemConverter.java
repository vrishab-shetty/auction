package me.vrishab.auction.item.converter;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.AuctionItemUpdateDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuctionItemUpdateToItemConverter implements Converter<AuctionItemUpdateDTO, Item> {

    @Override
    public Item convert(AuctionItemUpdateDTO source) {

        Item item = new Item();

        item.setId(source.id() == null ? null : UUID.fromString(source.id()));
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
