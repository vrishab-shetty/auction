package me.vrishab.auction.auction.exception;

public class ConcurrentBidException extends RuntimeException {
    public ConcurrentBidException() {
        super("Could not acquire lock for bidding. Please try again.");
    }
}
