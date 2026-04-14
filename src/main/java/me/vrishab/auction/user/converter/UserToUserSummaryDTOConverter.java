package me.vrishab.auction.user.converter;

import me.vrishab.auction.user.dto.UserSummaryDTO;
import me.vrishab.auction.user.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserSummaryDTOConverter implements Converter<User, UserSummaryDTO> {

    @Override
    public UserSummaryDTO convert(User source) {
        if (source == null) {
            return null;
        }
        return new UserSummaryDTO(
                source.getId(),
                source.getName(),
                source.getEmail()
        );
    }
}
