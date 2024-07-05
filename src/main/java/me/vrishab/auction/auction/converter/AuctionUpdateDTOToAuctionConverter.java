package me.vrishab.auction.auction.converter;

import lombok.NonNull;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.dto.AuctionUpdateDTO;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.converter.AuctionItemUpdateToItemConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuctionUpdateDTOToAuctionConverter implements Converter<AuctionUpdateDTO, Auction> {

    private final AuctionItemUpdateToItemConverter auctionItemUpdateToItemConverter;

    public AuctionUpdateDTOToAuctionConverter(AuctionItemUpdateToItemConverter auctionItemUpdateToItemConverter) {
        this.auctionItemUpdateToItemConverter = auctionItemUpdateToItemConverter;
    }

    @Override
    public @NonNull Auction convert(AuctionUpdateDTO source) {


        Set<Item> items = source
                .items()
                .stream().map(auctionItemUpdateToItemConverter::convert)
                .collect(Collectors.toSet());

        Auction auction = new Auction();
        auction.setName(source.name());
        auction.setStartTime(source.startTime());
        auction.setEndTime(source.endTime());
        auction.addAllItems(items);

        return auction;
    }
}
