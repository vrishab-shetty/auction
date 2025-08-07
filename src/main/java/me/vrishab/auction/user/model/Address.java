package me.vrishab.auction.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.vrishab.auction.user.converter.ZipcodeConverter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
public class Address {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 5)
    @Convert(converter = ZipcodeConverter.class)
    private Zipcode zipcode;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    public String getZipcode() {
        return this.zipcode.getValue();
    }
}
