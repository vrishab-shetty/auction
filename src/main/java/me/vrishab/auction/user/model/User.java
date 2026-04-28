package me.vrishab.auction.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@AllArgsConstructor
@Table(name = "\"USER\"")
public class User {

    @Id
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    private String name;

    private String description;

    private String password;

    private Boolean enabled;

    @Column(unique = true)
    private String email;

    private String contact;

    private Address homeAddress;

    public String getHomeZipCode() {
        return this.homeAddress.getZipcode();
    }

    public String getHomeStreet() {
        return this.homeAddress.getStreet();
    }

    public String getHomeCity() {
        return this.homeAddress.getCity();
    }

    public String getHomeCountry() {
        return this.homeAddress.getCountry();
    }

}