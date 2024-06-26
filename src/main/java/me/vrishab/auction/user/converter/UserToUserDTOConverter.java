package me.vrishab.auction.user.converter;

import lombok.NonNull;
import me.vrishab.auction.user.dto.UserDTO;
import me.vrishab.auction.user.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDTOConverter implements Converter<User, UserDTO> {


    @Override
    public @NonNull UserDTO convert(User source) {
        return new UserDTO(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getEmail(),
                source.getContact(),
                source.getEnabled(),
                source.getHomeAddress()
        );
    }
}
