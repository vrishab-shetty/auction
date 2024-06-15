package me.vrishab.auction.auction;

import me.vrishab.auction.system.exception.ObjectBadRequestException;
import me.vrishab.auction.system.exception.ObjectForbiddenException;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import me.vrishab.auction.system.exception.ObjectUnauthorizedException;

import java.util.UUID;

public class AuctionException {

    public static class AuctionNotFoundByIdException extends ObjectNotFoundException {

        public AuctionNotFoundByIdException(UUID id) {
            super("Could not find auction with Id " + id);
        }

    }

    public static class AuctionForbiddenUpdateException extends ObjectForbiddenException {

        public AuctionForbiddenUpdateException(UUID auctionId) {
            super("Provided Auction with id " + auctionId + " has already began or ended and cannot be modified");
        }

    }

    public static class AuctionForbiddenBidingPhaseException extends ObjectForbiddenException {
        public AuctionForbiddenBidingPhaseException(UUID id) {
            super("Auction with Id " + id + " is not in Biding Phase");
        }
    }

    public static class InvalidBidAmountException extends ObjectBadRequestException {
        public InvalidBidAmountException() {
            super("Bid amount must be higher than the current bid");
        }
    }

    public static class UnauthorizedAuctionAccess extends ObjectUnauthorizedException {

        public UnauthorizedAuctionAccess(boolean owner) {
            super(
                    owner ?
                            "The user is an owner of the auction" :
                            "The user not an owner of the auction"
            );
        }
    }
}
