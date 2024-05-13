package me.vrishab.auction.user.converter;



import lombok.NonNull;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.dto.UserCreationDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserCreationToUserConverter implements Converter<UserCreationDTO, User> {

    @Override
    public User convert(@NonNull UserCreationDTO source) {

        User user = new User();
        user.setName(source.name());
        user.setDescription(source.description());
        user.setEmail(source.email());
        user.setContact(source.contact());
        user.setPassword(source.password());
        user.setEnabled(true);

        return user;
    }

}
