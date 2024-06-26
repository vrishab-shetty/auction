package me.vrishab.auction.user.model;

import lombok.Data;
import me.vrishab.auction.user.UserException;

import java.util.regex.Pattern;

@Data
public abstract class Zipcode {

    protected Zipcode(String value) {
        if(validate(value)) {
            this.value = value;
        } else {
            throw new UserException.InvalidZipcodeException(value);
        }

    }

    private String value;

    public abstract boolean validate(String value);


}
