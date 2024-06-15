package me.vrishab.auction.system.exception;

public abstract class ObjectUnauthorizedException extends RuntimeException {

    protected ObjectUnauthorizedException(String message) {
        super(message);
    }

}
