package me.vrishab.auction.user.model;

import java.util.regex.Pattern;

public class GermanZipcode extends Zipcode {

    public static final Pattern PATTERN = Pattern.compile("\\d{5}");

    public GermanZipcode(String code) {
        super(code);
    }

    @Override
    public boolean validate(String value) {
        return PATTERN.matcher(value).matches();
    }

}
