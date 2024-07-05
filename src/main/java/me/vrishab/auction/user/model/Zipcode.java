package me.vrishab.auction.user.model;

import lombok.Data;
import me.vrishab.auction.user.UserException;

@Data
public abstract class Zipcode {

    private String value;

    protected Zipcode(String value) {
        if (validate(value)) {
            this.value = value;
        } else {
            throw new UserException.InvalidZipcodeException(value);
        }

    }

    public abstract boolean validate(String value);


}
