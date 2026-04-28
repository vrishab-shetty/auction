package me.vrishab.auction.user;

import me.vrishab.auction.system.exception.ObjectBadRequestException;
import me.vrishab.auction.system.exception.ObjectForbiddenException;
import me.vrishab.auction.system.exception.ObjectNotFoundException;

import java.util.UUID;

public class UserException {

    public static class UserNotFoundByIdException extends ObjectNotFoundException {

        public UserNotFoundByIdException(UUID id) {
            super("Could not find user with Id " + id);
        }

    }

    public static class UserNotFoundByUsernameException extends ObjectNotFoundException {
        public UserNotFoundByUsernameException(String username) {
            super("Could not find user with username " + username);
        }
    }

    public static class UserEmailAlreadyExistException extends ObjectBadRequestException {
        public UserEmailAlreadyExistException(String email) {
            super("The email " + email + " already exist");
        }
    }

    public static class InvalidZipcodeException extends ObjectBadRequestException {

        public InvalidZipcodeException(String unknownZipCode) {
            super("The zipcode " + unknownZipCode + " is invalid");
        }
    }

    public static class IncorrectPasswordException extends ObjectBadRequestException {
        public IncorrectPasswordException() {
            super("The current password provided is incorrect.");
        }
    }

    public static class BillingDetailsNotFoundByIdException extends ObjectNotFoundException {
        public BillingDetailsNotFoundByIdException(UUID id) {
            super("Could not find billing details with Id " + id);
        }
    }

    public static class UnauthorizedBillingDetailsAccessException extends ObjectForbiddenException {
        public UnauthorizedBillingDetailsAccessException() {
            super("You are not allowed to access this billing detail");
        }
    }

    public static class UserHasActiveAuctionsException extends ObjectBadRequestException {
        public UserHasActiveAuctionsException(UUID id) {
            super("Cannot delete user " + id + " while they have active or scheduled auctions");
        }
    }

}
