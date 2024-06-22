package me.vrishab.auction.user.model;

import java.util.regex.Pattern;

public class UKZipcode extends Zipcode {

    public static final Pattern PATTERN = Pattern.compile("^[A-Z]{1,2}\\d[A-Z\\d]? ?\\d[A-Z]{2}$", Pattern.CASE_INSENSITIVE);

    public UKZipcode(String code) {
        super(code);
    }

    @Override
    public boolean validate(String value) {
        return PATTERN.matcher(value).matches();
    }

}
