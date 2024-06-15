package me.vrishab.auction.system.exception;

import lombok.Getter;

@Getter
public enum Entity {
    AUCTION("auction"), ITEM("item"), USER("user");

    private final String name;

    Entity(String name) {
        this.name = name;
    }

}
