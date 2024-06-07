package me.vrishab.auction.system.exception;

import java.util.UUID;

public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String objectName, UUID id) {
        super("Could not find " + objectName + " with Id " + id.toString());
    }
}
