package me.vrishab.auction.item;

import me.vrishab.auction.system.exception.ObjectNotFoundException;

import java.util.UUID;

public class ItemException extends Exception{

    public static class ItemNotFoundByIdException extends ObjectNotFoundException {

        public ItemNotFoundByIdException(UUID id) {
            super("Could not find item with Id " + id);
        }

    }
}
