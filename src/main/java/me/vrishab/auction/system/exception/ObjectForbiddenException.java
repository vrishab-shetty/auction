package me.vrishab.auction.system.exception;

public abstract class ObjectForbiddenException extends RuntimeException {

    protected ObjectForbiddenException(String message) {
        super(message);
    }
}
