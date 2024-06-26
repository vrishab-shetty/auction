package me.vrishab.auction.user.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.vrishab.auction.user.model.Zipcode;
import org.springframework.stereotype.Component;

@Component
@Converter
public class ZipcodeConverter implements AttributeConverter<Zipcode, String> {

    private final StringToZipcodeConverter stringToZipcodeConverter;

    public ZipcodeConverter(StringToZipcodeConverter stringToZipcodeConverter) {
        this.stringToZipcodeConverter = stringToZipcodeConverter;
    }

    @Override
    public String convertToDatabaseColumn(Zipcode zipcode) {
        return zipcode.getValue();
    }

    @Override
    public Zipcode convertToEntityAttribute(String code) {
        return stringToZipcodeConverter.convert(code);
    }


}
