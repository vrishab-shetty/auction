package me.vrishab.auction.item;

public class ItemBadRequestException extends IllegalArgumentException {

    public ItemBadRequestException(String message) {
        super(message);
    }
}
