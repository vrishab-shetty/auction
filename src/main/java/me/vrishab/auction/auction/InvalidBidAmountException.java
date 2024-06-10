package me.vrishab.auction.auction;

public class InvalidBidAmountException extends RuntimeException {
    public InvalidBidAmountException() {
        super("Bid amount must be higher than the current bid");
    }
}
