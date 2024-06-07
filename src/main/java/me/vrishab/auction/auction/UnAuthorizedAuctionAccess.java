package me.vrishab.auction.auction;

public class UnAuthorizedAuctionAccess extends RuntimeException {

    public UnAuthorizedAuctionAccess() {
        super("The user not an owner of the auction");
    }
}

