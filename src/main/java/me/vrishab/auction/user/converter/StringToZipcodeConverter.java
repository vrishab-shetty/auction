package me.vrishab.auction.user.converter;

import me.vrishab.auction.user.UserException.InvalidZipcodeException;
import me.vrishab.auction.user.model.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToZipcodeConverter implements Converter<String, Zipcode> {
    @Override
    public Zipcode convert(String code) {
        if (code.matches(USZipcode.PATTERN.pattern())) {
            return new USZipcode(code);
        } else if (code.matches(GermanZipcode.PATTERN.pattern())) {
            return new GermanZipcode(code);
        } else if (code.matches(SwissZipcode.PATTERN.pattern())) {
            return new SwissZipcode(code);
        } else if (code.matches(UKZipcode.PATTERN.pattern())) {
            return new UKZipcode(code);
        } else {
            throw new InvalidZipcodeException(code);
        }
    }
}
