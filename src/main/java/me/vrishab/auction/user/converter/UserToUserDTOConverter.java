package me.vrishab.auction.user.converter;

import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.dto.UserDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserToUserDTOConverter implements Converter<User, UserDTO> {


    @Override
    public UserDTO convert(User source) {
        return new UserDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getEmail(),
                source.getContact(),
                source.isEnabled()
        );
    }
}
