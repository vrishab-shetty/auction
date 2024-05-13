package me.vrishab.auction.item;

public class ItemNotFoundException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public ItemNotFoundException(String itemId) {
        super("Could not find item with Id "+itemId);
    }
}
