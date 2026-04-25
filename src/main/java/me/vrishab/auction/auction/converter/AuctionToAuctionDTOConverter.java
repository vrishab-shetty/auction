package me.vrishab.auction.auction.converter;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.dto.AuctionDTO;
import me.vrishab.auction.item.converter.ItemToAuctionItemDTO;
import me.vrishab.auction.item.dto.AuctionItemDTO;
import me.vrishab.auction.user.converter.UserToUserSummaryDTOConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuctionToAuctionDTOConverter implements Converter<Auction, AuctionDTO> {

    private final ItemToAuctionItemDTO auctionItemToAuctionItemDTO;
    private final UserToUserSummaryDTOConverter userToUserSummaryDTOConverter;

    public AuctionToAuctionDTOConverter(ItemToAuctionItemDTO auctionItemToAuctionItemDTO, UserToUserSummaryDTOConverter userToUserSummaryDTOConverter) {
        this.auctionItemToAuctionItemDTO = auctionItemToAuctionItemDTO;
        this.userToUserSummaryDTOConverter = userToUserSummaryDTOConverter;
    }

    @Override
    public AuctionDTO convert(Auction source) {

        List<AuctionItemDTO> itemDTOs = source.getItems()
                .stream().map(auctionItemToAuctionItemDTO::convert)
                .toList();

        return new AuctionDTO(
                source.getId(),
                source.getName(),
                source.getStartTime(),
                source.getEndTime(),
                source.getStatus(),
                itemDTOs,
                userToUserSummaryDTOConverter.convert(source.getUser())
        );
    }
}
