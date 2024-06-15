package me.vrishab.auction.system.exception;

public abstract class ObjectNotFoundException extends RuntimeException {

    protected ObjectNotFoundException(String message) {
        super(message);
    }

}
