package me.vrishab.auction.user.converter;


import lombok.NonNull;
import me.vrishab.auction.user.User;
import me.vrishab.auction.user.dto.UserEditableDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserCreationToUserConverter implements Converter<UserEditableDTO, User> {

    @Override
    public @NonNull User convert(@NonNull UserEditableDTO source) {

        User user = new User();
        user.setName(source.name());
        user.setDescription(source.description());
        user.setEmail(source.email());
        user.setContact(source.contact());
        user.setPassword(source.password());
        user.setEnabled(source.enabled() == null || source.enabled());

        return user;
    }

}
