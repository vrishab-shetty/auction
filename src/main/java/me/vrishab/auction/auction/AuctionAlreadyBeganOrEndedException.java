package me.vrishab.auction.auction;

public class AuctionAlreadyBeganOrEndedException extends RuntimeException {
    public AuctionAlreadyBeganOrEndedException(String auctionId) {
        super("Provided Auction with id " + auctionId + " has already began or ended and cannot be modified");
    }
}
