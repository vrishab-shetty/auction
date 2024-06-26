package me.vrishab.auction.user.converter;


import lombok.NonNull;
import me.vrishab.auction.user.dto.UserEditableDTO;
import me.vrishab.auction.user.model.Address;
import me.vrishab.auction.user.model.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserEditableToUserConverter implements Converter<UserEditableDTO, User> {

    private final StringToZipcodeConverter stringToZipcodeConverter;

    public UserEditableToUserConverter(StringToZipcodeConverter stringToZipcodeConverter) {
        this.stringToZipcodeConverter = stringToZipcodeConverter;
    }


    @Override
    public @NonNull User convert(@NonNull UserEditableDTO source) {

        Address address = new Address(
                source.street(),
                stringToZipcodeConverter.convert(source.zipCode()),
                source.city(),
                source.country()
        );

        User user = new User();
        user.setName(source.name());
        user.setDescription(source.description());
        user.setEmail(source.email());
        user.setContact(source.contact());
        user.setPassword(source.password());
        user.setHomeAddress(address);
        user.setEnabled(true);

        return user;
    }

}
