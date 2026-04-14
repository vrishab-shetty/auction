package me.vrishab.auction.item.converter;

import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.dto.ItemDTO;
import me.vrishab.auction.user.converter.UserToUserSummaryDTOConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ItemToItemDTOConverter implements Converter<Item, ItemDTO> {

    private final UserToUserSummaryDTOConverter userToUserSummaryDTOConverter;

    public ItemToItemDTOConverter(UserToUserSummaryDTOConverter userToUserSummaryDTOConverter) {
        this.userToUserSummaryDTOConverter = userToUserSummaryDTOConverter;
    }

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
                userToUserSummaryDTOConverter.convert(source.getBuyer()),
                userToUserSummaryDTOConverter.convert(source.getAuction() != null ? source.getAuction().getUser() : null),
                source.getPopularity(),
                source.getAuction() != null ? source.getAuction().getId() : null
        );
    }
}
