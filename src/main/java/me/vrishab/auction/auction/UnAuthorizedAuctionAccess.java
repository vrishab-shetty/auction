package me.vrishab.auction.auction;

public class UnAuthorizedAuctionAccess extends RuntimeException {

    public UnAuthorizedAuctionAccess(boolean owner) {
        super(
                owner ?
                "The user is an owner of the auction" :
                "The user not an owner of the auction"
        );
    }
}

