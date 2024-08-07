package me.vrishab.auction.item.converter;

import lombok.NonNull;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.AuctionItemDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemToAuctionItemDTO implements Converter<Item, AuctionItemDTO> {
    @Override
    public @NonNull AuctionItemDTO convert(Item source) {
        return new AuctionItemDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getLocation(),
                source.getInitialPrice(),
                source.getCurrentBid(),
                source.getImageUrls(),
                source.getLegitimacyProof(),
                source.getExtras(),
                source.getBuyerEmail()
        );
    }
}
