package me.vrishab.auction.system.exception;

import java.util.UUID;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(Entity object, UUID id) {
        super("Could not find " + object.getName() + " with Id " + id.toString());
    }
}
