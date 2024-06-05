package me.vrishab.auction.auction.converter;

import lombok.NonNull;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.dto.AuctionEditableDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.converter.ItemEditableDTOToItemConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuctionEditableDTOToAuctionConverter implements Converter<AuctionEditableDTO, Auction> {

    private final ItemEditableDTOToItemConverter itemEditableDTOToItemConverter;

    public AuctionEditableDTOToAuctionConverter(ItemEditableDTOToItemConverter itemEditableDTOToItemConverter) {
        this.itemEditableDTOToItemConverter = itemEditableDTOToItemConverter;
    }

    @Override
    public @NonNull Auction convert(AuctionEditableDTO source) {

        Set<Item> items = source
                .items()
                .stream().map(itemEditableDTOToItemConverter::convert)
                .collect(Collectors.toSet());

        Auction auction = new Auction();
        auction.setName(source.name());
        auction.setStartTime(source.startTime());
        auction.setEndTime(source.endTime());
        auction.setBuyer(null);
        auction.setInitialPrice(source.initialPrice());
        auction.setItems(items);
        return auction;
    }

}
