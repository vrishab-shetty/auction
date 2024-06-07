package me.vrishab.auction.auction.converter;

import lombok.NonNull;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.dto.AuctionCreationDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.converter.ItemCreationDTOToItemConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuctionCreationDTOToAuctionConverter implements Converter<AuctionCreationDTO, Auction> {

    private final ItemCreationDTOToItemConverter itemCreationDTOToItemConverter;

    public AuctionCreationDTOToAuctionConverter(ItemCreationDTOToItemConverter itemCreationDTOToItemConverter) {
        this.itemCreationDTOToItemConverter = itemCreationDTOToItemConverter;
    }

    @Override
    public @NonNull Auction convert(AuctionCreationDTO source) {

        Set<Item> items = source
                .items()
                .stream().map(itemCreationDTOToItemConverter::convert)
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
