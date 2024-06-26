package me.vrishab.auction.user.model;

import java.util.regex.Pattern;

public class SwissZipcode extends Zipcode {

    public static final Pattern PATTERN = Pattern.compile("\\d{4}");

    public SwissZipcode(String value) {
        super(value);
    }

    @Override
    public boolean validate(String value) {
        return PATTERN.matcher(value).matches();
    }


}
