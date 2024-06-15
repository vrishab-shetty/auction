package me.vrishab.auction.system.exception;

public abstract class ObjectBadRequestException extends RuntimeException {

    protected ObjectBadRequestException(String message) {
        super(message);
    }
}
