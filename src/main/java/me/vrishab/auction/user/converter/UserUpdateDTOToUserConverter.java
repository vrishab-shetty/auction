package me.vrishab.auction.user.converter;

import lombok.NonNull;
import me.vrishab.auction.user.dto.UserUpdateDTO;
import me.vrishab.auction.user.model.Address;
import me.vrishab.auction.user.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserUpdateDTOToUserConverter implements Converter<UserUpdateDTO, User> {

    private final StringToZipcodeConverter stringToZipcodeConverter;

    public UserUpdateDTOToUserConverter(StringToZipcodeConverter stringToZipcodeConverter) {
        this.stringToZipcodeConverter = stringToZipcodeConverter;
    }

    @Override
    public @NonNull User convert(@NonNull UserUpdateDTO source) {
        Address address = new Address(
                source.street(),
                stringToZipcodeConverter.convert(source.zipCode()),
                source.city(),
                source.country()
        );

        User user = new User();
        user.setName(source.name());
        user.setDescription(source.description());
        user.setContact(source.contact());
        user.setHomeAddress(address);
        user.setEnabled(true);

        return user;
    }
}
