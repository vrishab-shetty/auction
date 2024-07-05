package me.vrishab.auction.item.converter;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.ItemDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemToItemDTOConverter implements Converter<Item, ItemDTO> {
    @Override
    public ItemDTO convert(Item source) {
        return new ItemDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getLocation(),
                source.getInitialPrice(),
                source.getCurrentBid(),
                source.getImageUrls(),
                source.getLegitimacyProof(),
                source.getExtras(),
                source.getBuyerEmail(),
                source.getSeller(),
                source.getPopularity()
        );
    }
}
