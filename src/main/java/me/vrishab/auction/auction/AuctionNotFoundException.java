package me.vrishab.auction.auction;

import java.util.UUID;

public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(UUID auctionId) {
        super("Could not find auction with Id " + auctionId);
    }
}
