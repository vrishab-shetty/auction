package me.vrishab.auction.auction;

public class AuctionForbiddenUpdateException extends RuntimeException {
    public AuctionForbiddenUpdateException(String auctionId) {
        super("Provided Auction with id " + auctionId + " has already began or ended and cannot be modified");
    }
}
