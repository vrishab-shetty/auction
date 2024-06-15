package me.vrishab.auction.user;

import me.vrishab.auction.system.exception.ObjectBadRequestException;
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
            super("Could find user with username " + username);
        }
    }

    public static class UserEmailAlreadyExistException extends ObjectBadRequestException {
        public UserEmailAlreadyExistException(String email) {
            super("The email " + email + " already exist");
        }
    }

}
