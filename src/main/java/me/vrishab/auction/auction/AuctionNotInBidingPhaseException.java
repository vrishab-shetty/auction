package me.vrishab.auction.auction;

import java.util.UUID;

public class AuctionNotInBidingPhaseException extends RuntimeException {
    public AuctionNotInBidingPhaseException(UUID id) {
        super("Auction with Id "+id+" is not in biding Phase");
    }
}
